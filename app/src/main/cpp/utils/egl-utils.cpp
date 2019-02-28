#include "egl-utils.h"

static const char *tag = "egl-utils";

void printEGLConfigs(EGLDisplay display, EGLConfig *configs, int len) {
    LOGD(tag, "Config number: %d", len);
    EGLint redSize;
    EGLint greenSize;
    EGLint blueSize;
    EGLint alphaSize;
    EGLint depthSize;
    EGLint stencilSize;
    for (int i = 0; i < len; ++i) {
        eglGetConfigAttrib(display, configs[i], EGL_RED_SIZE, &redSize);
        eglGetConfigAttrib(display, configs[i], EGL_GREEN_SIZE, &greenSize);
        eglGetConfigAttrib(display, configs[i], EGL_BLUE_SIZE, &blueSize);
        eglGetConfigAttrib(display, configs[i], EGL_ALPHA_SIZE, &alphaSize);
        eglGetConfigAttrib(display, configs[i], EGL_DEPTH_SIZE, &depthSize);
        eglGetConfigAttrib(display, configs[i], EGL_STENCIL_SIZE, &stencilSize);
        LOGW(tag, "EGL Config %d: R(%d) G(%d) B(%d) A(%d) depth(%d) stencil(%d)",
             i,
             redSize,
             greenSize,
             blueSize,
             alphaSize,
             depthSize,
             stencilSize);
    }
}

EGLBoolean chooseEGLConfig(EGLDisplay display, EGLint r, EGLint g, EGLint b, EGLint a, EGLint depth,
                           EGLint stencil) {
    EGLint attrs[] = {
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_RED_SIZE, r,
            EGL_GREEN_SIZE, g,
            EGL_BLUE_SIZE, b,
            EGL_ALPHA_SIZE, a,
            EGL_DEPTH_SIZE, depth,
            EGL_STENCIL_SIZE, stencil,
            EGL_NONE
    };

    EGLint num;
    EGLConfig config;
    return eglChooseConfig(display, attrs, &config, 1, &num);
}

