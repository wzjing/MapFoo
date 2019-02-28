cmake_minimum_required(VERSION 3.4.1)

#设置编译指令
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fno-rtti -fno-exceptions -Wall")

#如果当前系统版本过低设置拒绝编译
if (${ANDROID_PLATFORM_LEVEL} LESS 11)
    message(FATAL_ERROR "OpenGL 2 is not supported before API level 11 (currently using ${ANDROID_PLATFORM_LEVEL}).")
    return()
elseif (${ANDROID_PLATFORM_LEVEL} LESS 18)
    add_definitions("-DDYNAMIC_ES3")
    set(OPENGL_LIB GLESv2)
else ()
    set(OPENGL_LIB GLESv3)
endif (${ANDROID_PLATFORM_LEVEL} LESS 11)

# Include libraries needed for Triangle lib
add_library(jni-renderer
        SHARED
        jni-renderer.cpp
        utils/android-utils.cpp
        utils/gl-utils.cpp
        utils/egl-utils.cpp)
target_link_libraries(jni-renderer
        ${OPENGL_LIB}
        jnigraphics
        android
        EGL
        log
        m)