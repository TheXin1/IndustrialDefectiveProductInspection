"""
AnomalyGPT 服务端 API
将异常检测与定位能力封装为 REST API，便于二次开发与集成。
运行方式: cd code && python api_server.py
"""
import os
import sys
import base64
import tempfile
import time
import threading
from io import BytesIO
from typing import Optional

import cv2
import numpy as np
import torch
from PIL import Image as PILImage
from matplotlib import pyplot as plt
from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

# 确保无论从项目根还是 code 目录启动，都能导入 model
_code_dir = os.path.dirname(os.path.abspath(__file__))
if _code_dir not in sys.path:
    sys.path.insert(0, _code_dir)

from model.openllama import OpenLLAMAPEFTModel


# ---------- 配置（可通过环境变量覆盖）-----------
def _path_from_env(key: str, default: str) -> str:
    base = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    return os.environ.get(key, os.path.join(base, default))


IMAGEBIND_CKPT = _path_from_env(
    "ANOMALYGPT_IMAGEBIND_CKPT",
    "pretrained_ckpt/imagebind_ckpt/imagebind_huge.pth",
)
VICUNA_CKPT = _path_from_env(
    "ANOMALYGPT_VICUNA_CKPT",
    "pretrained_ckpt/vicuna_ckpt/7b_v0",
)
ANOMALYGPT_CKPT = _path_from_env(
    "ANOMALYGPT_CKPT",
    "code/ckpt/train_supervised/pytorch_model.pt",
)
DELTA_CKPT = _path_from_env(
    "ANOMALYGPT_DELTA_CKPT",
    "pretrained_ckpt/pandagpt_ckpt/7b/pytorch_model.pt",
)

# 若 ANOMALYGPT_CKPT 为相对路径，则基于项目根目录解析
if not os.path.isabs(ANOMALYGPT_CKPT):
    _root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    ANOMALYGPT_CKPT = os.path.join(_root, ANOMALYGPT_CKPT)

MODEL_ARGS = {
    "model": "openllama_peft",
    "imagebind_ckpt_path": IMAGEBIND_CKPT,
    "vicuna_ckpt_path": VICUNA_CKPT,
    "anomalygpt_ckpt_path": ANOMALYGPT_CKPT,
    "delta_ckpt_path": DELTA_CKPT,
    "stage": 2,
    "max_tgt_len": 128,
    "lora_r": 32,
    "lora_alpha": 32,
    "lora_dropout": 0.1,
}

# ---------- 全局模型（懒加载）-----------
_model: Optional[OpenLLAMAPEFTModel] = None
_model_lock = threading.Lock()

# 启动加载进度（用于控制台显示/可选查询）
_startup_status = {
    "state": "not_started",  # not_started | loading | ready | error
    "step": 0,
    "total": 6,
    "message": "",
    "started_at": None,
    "finished_at": None,
    "error": None,
}


def _env_flag(key: str, default: str = "1") -> bool:
    v = os.environ.get(key, default).strip().lower()
    return v not in ("0", "false", "no", "off", "")


def _update_startup_status(*, step: int, message: str, state: Optional[str] = None, error: Optional[str] = None) -> None:
    if state is not None:
        _startup_status["state"] = state
    _startup_status["step"] = int(step)
    _startup_status["message"] = str(message)
    if error is not None:
        _startup_status["error"] = str(error)

    # 控制台进度显示（按阶段百分比，而非文件级真实进度）
    if _env_flag("ANOMALYGPT_SHOW_LOAD_PROGRESS", "1"):
        total = int(_startup_status["total"]) or 1
        pct = int(round(100 * min(max(step, 0), total) / total))
        started_at = _startup_status.get("started_at")
        elapsed = ""
        if isinstance(started_at, (int, float)):
            elapsed = f", elapsed={time.time() - started_at:.1f}s"
        print(f"[startup] {step}/{total} ({pct}%) {message}{elapsed}", flush=True)


def get_model() -> OpenLLAMAPEFTModel:
    global _model
    if _model is not None:
        return _model

    with _model_lock:
        if _model is not None:
            return _model

        # 初始化启动状态（即使不是 startup 事件触发，也能显示首次加载进度）
        if _startup_status["state"] in ("not_started", "error"):
            _startup_status["started_at"] = time.time()
            _startup_status["finished_at"] = None
            _startup_status["error"] = None
            _update_startup_status(step=0, message="开始加载模型", state="loading")

        try:
            _update_startup_status(step=1, message="检查运行环境与权重文件")
            if not torch.cuda.is_available():
                raise RuntimeError("未检测到 CUDA/GPU。当前实现需要 GPU 才能加载并推理（torch.cuda.is_available()=False）。")

            for k in ("imagebind_ckpt_path", "vicuna_ckpt_path", "anomalygpt_ckpt_path", "delta_ckpt_path"):
                p = MODEL_ARGS.get(k)
                if not p:
                    raise RuntimeError(f"缺少必要配置: {k}")
                # vicuna_ckpt_path 通常是目录（HF 权重），其余是文件
                if k == "vicuna_ckpt_path":
                    if not os.path.isdir(p):
                        raise FileNotFoundError(f"未找到语言模型目录: {p}")
                else:
                    if not os.path.isfile(p):
                        raise FileNotFoundError(f"未找到权重文件: {p}")

            _update_startup_status(step=2, message="初始化模型结构（视觉编码器 + 语言解码器）")
            model = OpenLLAMAPEFTModel(**MODEL_ARGS)

            _update_startup_status(step=3, message="加载 stage1 delta 权重")
            delta_ckpt = torch.load(MODEL_ARGS["delta_ckpt_path"], map_location=torch.device("cpu"))
            model.load_state_dict(delta_ckpt, strict=False)

            _update_startup_status(step=4, message="加载 AnomalyGPT 监督微调权重")
            anomalygpt_ckpt = torch.load(MODEL_ARGS["anomalygpt_ckpt_path"], map_location=torch.device("cpu"))
            model.load_state_dict(anomalygpt_ckpt, strict=False)

            _update_startup_status(step=5, message="切换到 eval + half，并迁移到 GPU")
            model = model.eval().half().cuda()

            _update_startup_status(step=6, message="模型加载完成", state="ready")
            _startup_status["finished_at"] = time.time()
            _model = model
            return _model
        except Exception as e:
            _update_startup_status(step=_startup_status.get("step", 0), message="模型加载失败", state="error", error=str(e))
            _startup_status["finished_at"] = time.time()
            raise


def postprocess_localization_map(pixel_output: torch.Tensor, query_image_path: str) -> str:
    """将模型输出的 pixel_output 转为 224x224 灰度图并做后处理，返回 PNG base64。"""
    buf = BytesIO()
    plt.imshow(
        pixel_output.to(torch.float16).reshape(224, 224).detach().cpu(),
        cmap="binary_r",
    )
    plt.axis("off")
    plt.savefig(buf, format="png", bbox_inches="tight", pad_inches=0)
    plt.close()
    buf.seek(0)

    target_size = 224
    with PILImage.open(buf) as heatmap:
        heatmap = heatmap.convert("L").resize((224, 224), PILImage.LANCZOS)

    try:
        with PILImage.open(query_image_path) as img:
            original_width, original_height = img.size
    except Exception:
        original_width, original_height = 224, 224

    if original_width > original_height:
        new_width = target_size
        new_height = int(target_size * (original_height / original_width))
    else:
        new_height = target_size
        new_width = int(target_size * (original_width / original_height))

    new_image = PILImage.new("L", (target_size, target_size), 255)
    paste_x = (target_size - new_width) // 2
    paste_y = (target_size - new_height) // 2
    heatmap_resized = heatmap.resize((new_width, new_height), PILImage.LANCZOS)
    new_image.paste(heatmap_resized, (paste_x, paste_y))

    out_buf = BytesIO()
    new_image.save(out_buf, format="png")
    image_np = np.array(new_image)
    kernel = np.ones((3, 3), np.uint8)
    eroded = cv2.erode(image_np, kernel, iterations=1)
    eroded_pil = PILImage.fromarray(eroded)
    out_buf2 = BytesIO()
    eroded_pil.save(out_buf2, format="png")
    return base64.b64encode(out_buf2.getvalue()).decode("utf-8")


# ---------- FastAPI ----------
app = FastAPI(
    title="AnomalyGPT API",
    description="工业异常检测与定位服务，基于 AnomalyGPT 大视觉语言模型",
    version="1.0.0",
)


@app.on_event("startup")
def _on_startup():
    """
    启动时预加载模型并输出加载进度。
    - ANOMALYGPT_PRELOAD_ON_STARTUP=0 可关闭预加载（仍保留懒加载）
    - ANOMALYGPT_PRELOAD_STRICT=0 时，预加载失败不会阻止服务启动（但接口会在首次推理时报错）
    """
    if not _env_flag("ANOMALYGPT_PRELOAD_ON_STARTUP", "1"):
        return

    _startup_status["started_at"] = time.time()
    _startup_status["finished_at"] = None
    _startup_status["error"] = None
    _update_startup_status(step=0, message="服务启动：预加载模型", state="loading")

    try:
        get_model()
    except Exception:
        if _env_flag("ANOMALYGPT_PRELOAD_STRICT", "1"):
            raise


class DetectResponse(BaseModel):
    success: bool = True
    data: dict = Field(..., description="检测结果")


@app.get("/api/v1/health")
def health():
    """健康检查，用于运维与负载均衡。"""
    try:
        m = get_model()
        return {"status": "ok", "model_loaded": m is not None}
    except Exception as e:
        return JSONResponse(
            status_code=503,
            content={"status": "error", "model_loaded": False, "detail": str(e)},
        )


@app.get("/api/v1/info")
def info():
    """返回当前使用的模型路径配置（不包含敏感信息）。"""
    return {
        "imagebind_ckpt": os.path.basename(MODEL_ARGS["imagebind_ckpt_path"]),
        "vicuna_ckpt": os.path.basename(MODEL_ARGS["vicuna_ckpt_path"]),
        "anomalygpt_ckpt": os.path.basename(MODEL_ARGS["anomalygpt_ckpt_path"]),
        "delta_ckpt": os.path.basename(MODEL_ARGS["delta_ckpt_path"]),
    }


@app.get("/api/v1/startup_status")
def startup_status():
    """查看服务启动/模型加载进度（阶段型百分比）。"""
    return _startup_status


@app.post("/api/v1/detect", response_model=DetectResponse)
async def detect(
    image: UploadFile = File(..., description="待检测图像"),
    normal_image: Optional[UploadFile] = File(None, description="可选正常参考图（few-shot）"),
    prompt: str = Form("Describe this image."),
    max_tgt_len: int = Form(128, ge=1, le=512),
    top_p: float = Form(0.01, ge=0.0, le=1.0),
    temperature: float = Form(1.0, ge=0.0, le=2.0),
):
    """
    异常检测：上传待检图像，可选正常图与文本 prompt，返回文字描述与异常定位图（base64）。
    """
    if not image.content_type or not image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="请上传图片文件（image/*）")

    suffix = ".jpg"
    if "png" in (image.content_type or ""):
        suffix = ".png"
    elif "webp" in (image.content_type or ""):
        suffix = ".webp"

    query_path = None
    normal_path = None
    try:
        content = await image.read()
        fd, query_path = tempfile.mkstemp(suffix=suffix)
        os.close(fd)
        with open(query_path, "wb") as f:
            f.write(content)

        if normal_image and normal_image.filename:
            nc = await normal_image.read()
            fd2, normal_path = tempfile.mkstemp(suffix=suffix)
            os.close(fd2)
            with open(normal_path, "wb") as f:
                f.write(nc)

        model = get_model()
        response_text, pixel_output = model.generate(
            {
                "prompt": prompt,
                "image_paths": [query_path],
                "normal_img_paths": [normal_path] if normal_path else [],
                "audio_paths": [],
                "video_paths": [],
                "thermal_paths": [],
                "top_p": top_p,
                "temperature": temperature,
                "max_tgt_len": max_tgt_len,
                "modality_embeds": [],
            },
            web_demo=True,
        )

        localization_b64 = postprocess_localization_map(pixel_output, query_path)
        has_anomaly = "normal" not in response_text.lower() or "anomaly" in response_text.lower() or "defect" in response_text.lower()
        return DetectResponse(
            success=True,
            data={
                "description": response_text,
                "localization_image_base64": localization_b64,
                "has_anomaly": has_anomaly,
            },
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"检测失败: {str(e)}")
    finally:
        for p in (query_path, normal_path):
            if p and os.path.isfile(p):
                try:
                    os.remove(p)
                except OSError:
                    pass


if __name__ == "__main__":
    import uvicorn
    port = int(os.environ.get("PORT", 6008))
    uvicorn.run(app, host="127.0.0.1", port=port)
