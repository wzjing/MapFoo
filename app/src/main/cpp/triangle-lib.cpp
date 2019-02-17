#include <jni.h>
#include <GLES3/gl3.h>
#include <android/log.h>
#include "utils/gl-utils.h"
#include "utils/native-utils.h"


extern "C" JNIEXPORT jboolean JNICALL Java_com_infinitytech_mapfoo_utils_TriangleLib_init(
        JNIEnv *env, jclass type);
extern "C" JNIEXPORT void JNICALL Java_com_infinitytech_mapfoo_utils_TriangleLib_resize(JNIEnv *env,
                                                                                        jclass type,
                                                                                        jint width,
                                                                                        jint height);
extern "C" JNIEXPORT void JNICALL Java_com_infinitytech_mapfoo_utils_TriangleLib_step(JNIEnv *env,
                                                                                      jclass type);

const char *tag = "TRIANGLE-LIB";
float vertexBuffer[8] = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 0.7f};

GLuint resolutionHandle;
GLuint timeHandle;
GLuint vertexHandle;
GLuint program;

float size[2];

jboolean Java_com_infinitytech_mapfoo_utils_TriangleLib_init(JNIEnv *env, jclass type) {
    const char *VERTEX_SHADER_CODE = loadAssetFile(env, "shader/vertex_shader.glsl");
    const char *FRAGMENT_SHADER_CODE = loadAssetFile(env, "shader/fragment_shader.glsl");
    program = createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
    if (!program) {
        LOGE(tag, "Failed to create program");
        return JNI_FALSE;
    }
    vertexHandle = (GLuint) glGetAttribLocation(program, "vertexPosition");
    timeHandle = (GLuint) glGetUniformLocation(program, "iTime");
    resolutionHandle = (GLuint) glGetUniformLocation(program, "iResolution");
//    glDisable(GL_CULL_FACE);
    glEnable(GL_BLEND);
    return JNI_TRUE;
}

void Java_com_infinitytech_mapfoo_utils_TriangleLib_resize(JNIEnv *env, jclass type, jint width,
                                                           jint height) {
    size[0] = width;
    size[1] = height;
}

void Java_com_infinitytech_mapfoo_utils_TriangleLib_step(JNIEnv *env, jclass type) {

    // Use Shader
    glUseProgram(program);

    // Shader: iTime
    glUniform1f(timeHandle, (float) clock() / CLOCKS_PER_SEC);

    // Shader: iResolution
    glUniform2f(resolutionHandle, size[0], size[1]);

    // Shader: vertexPosition
    glEnableVertexAttribArray(vertexHandle);
    glVertexAttribPointer(vertexHandle, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), vertexBuffer);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

}