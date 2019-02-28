#include <jni.h>
#include <GLES3/gl3.h>
#include <android/log.h>
#include <EGL/egl.h>
#include "utils/gl-utils.h"
#include "utils/android-utils.h"
#include "utils/egl-utils.h"

bool init(JNIEnv *env);

void resize(int width, int height);

void step();

#define JNI_FUNC(func) Java_com_infinitytech_mapfoo_ui_map_JNIRenderer_##func

extern "C" {
JNIEXPORT void JNICALL JNI_FUNC(onSurfaceCreatedNative)(JNIEnv *env, jobject obj) {
    init(env);
}
JNIEXPORT void JNICALL JNI_FUNC(onSurfaceChangedNative)(JNIEnv *env, jobject obj,
                                                        jint width, jint height) {
    resize(width, height);
}

JNIEXPORT void JNICALL JNI_FUNC(onDrawFrameNative)(JNIEnv *env, jobject obj) {
    step();
}
}


typedef struct Frame {
    int w;
    int h;
    void *pixels;
} Frame;

static const char *tag = "jni-renderer";
float vertexBuffer[8] = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};

GLuint resolutionHandle;
GLuint timeHandle;
GLuint vertexHandle;
GLuint program;
GLuint sampler2DHandle;

GLuint texture;

static Frame frame;

float size[2];

EGLDisplay eglDisplay;


bool init(JNIEnv *env) {

    eglDisplay = eglGetCurrentDisplay();

    EGLint configSuccess = chooseEGLConfig(eglDisplay, 8, 8, 8, 8, 24, 8);
    if (configSuccess) {
        LOGD(tag, "choose config success");
    } else {
        LOGD(tag, "choose config fail");
    }


    Bitmap *mBitmap = getBitmapByName(env, "test.png");
    frame.w = mBitmap->width;
    frame.h = mBitmap->height;
    frame.pixels = mBitmap->pixels;

    const char *VERTEX_SHADER_CODE = loadAssetFile(env, "shader/vertex_shader.glsl");
    const char *FRAGMENT_SHADER_CODE = loadAssetFile(env, "shader/fragment_snow.glsl");
    program = createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
    if (!program) {
        LOGE(tag, "Failed to create program");
        return JNI_FALSE;
    }
    vertexHandle = (GLuint) glGetAttribLocation(program, "vertexPosition");
    timeHandle = (GLuint) glGetUniformLocation(program, "iTime");
    resolutionHandle = (GLuint) glGetUniformLocation(program, "iResolution");
    sampler2DHandle = (GLuint) glGetUniformLocation(program, "iChannel0");

    //Init Texture parameters
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);


    checkGlError("set alpha");

    //Configure the texture data
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, frame.w, frame.h, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                 frame.pixels);
    glGenerateMipmap(GL_TEXTURE_2D);

    return JNI_TRUE;
}

void resize(int width, int height) {
    size[0] = width;
    size[1] = height;
    glViewport(0, 0, width, height);
}

void step() {

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glDisable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    // Use Shader
    glUseProgram(program);

    // Shader: iChannel0
    glUniform1i(sampler2DHandle, texture);

    // Shader: iTime
    glUniform1f(timeHandle, (float) clock() / CLOCKS_PER_SEC);

    // Shader: iResolution
    glUniform2f(resolutionHandle, size[0], size[1]);

    // Shader: vertexPosition
    glEnableVertexAttribArray(vertexHandle);
    glVertexAttribPointer(vertexHandle, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), vertexBuffer);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
}