//
// Created by wzjing on 2019/2/18.
//

#ifndef MAPFOO_EGL_UTILS_H
#define MAPFOO_EGL_UTILS_H

#include <EGL/egl.h>
#include "android-utils.h"

void printEGLConfigs(EGLDisplay display, EGLConfig *configs, int len);

EGLBoolean chooseEGLConfig(EGLDisplay display, EGLint r, EGLint g, EGLint b, EGLint a, EGLint depth,
                     EGLint stencil);

#endif //MAPFOO_EGL_UTILS_H
