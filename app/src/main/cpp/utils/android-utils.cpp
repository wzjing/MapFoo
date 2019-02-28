#include <android/bitmap.h>
#include "android-utils.h"

static const char *tag = "android-utils";

char *loadAssetFile(JNIEnv *env, const char *filename) {
    AAssetManager *mgr = getAssetManager(env);
    AAsset *asset = AAssetManager_open(mgr, filename, AASSET_MODE_UNKNOWN);
    if (asset == NULL) {
        LOGE(tag, "Asset not found");
        exit(0);
    }
    long size = AAsset_getRemainingLength(asset);
    char *buffer = (char *) malloc(sizeof(char) * size);
    memset(buffer, 0, size);
    int ret = AAsset_read(asset, buffer, size);
    buffer[size] = '\0';\
    if (ret < 0) {
        LOGE(tag, "Read assset error: %d", ret);
        exit(0);
    }
    AAsset_close(asset);
    return buffer;
}

AAssetManager *getAssetManager(JNIEnv *env) {
    jclass AndroidContext = env->FindClass("android/content/Context");
    jobject androidContext = getCurrentContext(env);
    jmethodID getAssets = env->GetMethodID(AndroidContext, "getAssets",
                                           "()Landroid/content/res/AssetManager;");
    return AAssetManager_fromJava(env, env->CallObjectMethod(androidContext, getAssets));
}

jobject getCurrentContext(JNIEnv *env) {
    jclass ActivityThread = env->FindClass("android/app/ActivityThread");
    jmethodID currentActivityThread = env->GetStaticMethodID(ActivityThread,
                                                             "currentActivityThread",
                                                             "()Landroid/app/ActivityThread;");
    jobject activityThread = env->CallStaticObjectMethod(ActivityThread, currentActivityThread);
    jmethodID getApplication = env->GetMethodID(ActivityThread, "getApplication",
                                                "()Landroid/app/Application;");
    return env->CallObjectMethod(activityThread, getApplication);
}

Bitmap *getBitmap(JNIEnv *env, jobject bitmap) {

    Bitmap *mBitmap = (Bitmap *) malloc(sizeof(Bitmap));

    AndroidBitmapInfo bmp_info = {0};
    if (AndroidBitmap_getInfo(env, bitmap, &bmp_info) < 0) {
        LOGD(tag, "nativeProcess(): Unable to get bitmap info");
        return 0;
    }
    LOGD(tag, "nativeProcess(): Bitmap Info: %d x %d <format: %d>", bmp_info.width,
         bmp_info.height,
         bmp_info.format);
    mBitmap->width = bmp_info.width;
    mBitmap->height = bmp_info.height;
    mBitmap->format = bmp_info.format;
    if (AndroidBitmap_lockPixels(env, bitmap, &mBitmap->pixels) < 0) {
        LOGE(tag, "nativeProcess(): Unable to lock bitmap pixels");
        return 0;
    }

    if (!mBitmap->pixels) {
        LOGE(tag, "nativeProcess(): didn't get any pixels");
        return 0;
    }

    LOGD(tag, "Bitmap Info: %d x %d", mBitmap->width, mBitmap->height);

    return mBitmap;
}

Bitmap *getBitmapByName(JNIEnv *env, const char *name) {
    jclass BitmapFactory = env->FindClass("android/graphics/BitmapFactory");
    jclass Context = env->FindClass("android/content/Context");
    jclass AssetManager = env->FindClass("android/content/res/AssetManager");
    jmethodID decodeStream = env->GetStaticMethodID(BitmapFactory, "decodeStream",
                                                    "(Ljava/io/InputStream;)Landroid/graphics/Bitmap;");
    jmethodID getAssets = env->GetMethodID(Context, "getAssets",
                                           "()Landroid/content/res/AssetManager;");
    jmethodID open = env->GetMethodID(AssetManager, "open",
                                      "(Ljava/lang/String;)Ljava/io/InputStream;");

    jobject context = getCurrentContext(env);
    jobject assets = env->CallObjectMethod(context, getAssets);
    jobject is = env->CallObjectMethod(assets, open, env->NewStringUTF(name));
    jobject bitmap = env->CallStaticObjectMethod(BitmapFactory, decodeStream, is);

    return getBitmap(env, bitmap);
}