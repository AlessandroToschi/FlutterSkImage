//
// Created by Alessandro Toschi on 19/04/22.
//

#ifndef ANDROID_C_HEADER_H
#define ANDROID_C_HEADER_H

#include <stdint.h>
#include <stdlib.h>

#include "jni.h"

struct t_result {
    const void* ptr;
    uint64_t width;
    uint64_t height;
    uint64_t bytesPerRow;
};

struct p_result {
    struct t_result* textures;
    uint64_t length;
};

JavaVM* currentJvm;
jmethodID getTextureIDMethod;
jclass mainActivityClass;
jobject mainActivityInstance;

/*
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {

    JNIEnv* currentEnv;
    jvm->GetEnv((void**)&currentEnv, JNI_VERSION_1_6);

    auto localMainActivityClass = currentEnv->FindClass("com/example/flutter_skimage/MainActivity");
    mainActivityClass = reinterpret_cast<jclass>(currentEnv->NewGlobalRef(localMainActivityClass));
    getTextureIDMethod = currentEnv->GetStaticMethodID(mainActivityClass, "getTextureID", "()[I");

    currentJvm = jvm;

    return JNI_VERSION_1_6;
}*/


extern "C" JNIEXPORT void JNICALL Java_com_example_flutter_1skimage_MainActivity_loadJNI(JNIEnv* env, jobject obj){
    mainActivityInstance = env->NewGlobalRef(obj);
    //localMainActivityClass = env->FindClass("com/example/flutter_skimage/MainActivity");
    getTextureIDMethod = env->GetMethodID(env->GetObjectClass(mainActivityInstance), "getTextureID", "()[I");
    //getTextureIDMethod = env->GetStaticMethodID(mainActivityClass, "getTextureID", "()Ljava/util/List;");
    //mainActivityObject = obj;
    env->GetJavaVM(&currentJvm);
}

extern "C" __attribute__((visibility("default"))) __attribute__((used))
p_result multiple_textures_bridge() {

    JNIEnv* currentEnv;
    currentJvm->AttachCurrentThread(&currentEnv, nullptr);

    auto result = (jintArray)currentEnv->CallObjectMethod(mainActivityInstance, getTextureIDMethod);
    //int textureID = currentEnv->CallIntMethod(currentEnv->GetObjectArrayElement(result, 0), intValue);
    //int width = currentEnv->CallIntMethod(currentEnv->GetObjectArrayElement(result, 1), intValue);
    //int height = currentEnv->CallIntMethod(currentEnv->GetObjectArrayElement(result, 2), intValue);
    jboolean isCopy = true;
    jint* integers = currentEnv->GetIntArrayElements(result, &isCopy);
    int textureID = integers[0];
    int width = integers[1];
    int height = integers[2];

    auto* t1 = (struct t_result*)malloc(sizeof(struct t_result));
    t1->ptr = (const void*)textureID;
    t1->width = width;
    t1->height = height;

    p_result p{};
    p.textures = t1;
    p.length = 1;
    currentJvm->DetachCurrentThread();
    return p;
}

#endif //ANDROID_C_HEADER_H
