#include <jni.h>
#include <string>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "Rom.h"
#include "Gameboy.h"
#include "AndroidGUI.h"
#include <thread>

static AndroidGUI* gui;
static JavaVM* jvm;
static jobject globalSurface;

static void render() {
    JNIEnv* env;
    jvm->AttachCurrentThread(&env, NULL);
    while(true) {
        gui->render(env);
    }
}

static void startEmulator(JNIEnv *env, jobject surface, jstring path) {
    env->GetJavaVM(&jvm);
    globalSurface = env->NewGlobalRef(surface);
    Rom rom(env->GetStringUTFChars(path, 0));
    gui = new AndroidGUI(env, globalSurface);
    struct config emulatorConfig;
    Gameboy gameboy(rom, (GUI*) gui, &emulatorConfig);
    std::thread first (render);
    gameboy.run();
}

static void throwJavaException(JNIEnv* env, std::string message) {
    jclass c = env->FindClass("com/matthew/mboy/NativeException");
    env->ThrowNew(c, message.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_matthew_mboy_MainActivity_startEmulator(
        JNIEnv* env,
        jobject /* this */,
        jobject surface,
        jstring path) {
    try {
        startEmulator(env, surface, path);
    } catch (std::invalid_argument e) {
        std::string errorMessage = "Bad argument.\n" + std::string(e.what());
        throwJavaException(env, errorMessage);
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_matthew_mboy_MainActivity_getFPS(
        JNIEnv* env,
        jobject /* this */) {
    return gui != nullptr ? gui->fps : 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_matthew_mboy_MainActivity_getRenderFPS(
        JNIEnv* env,
        jobject /* this */) {
    return gui != nullptr ? gui->renderFps : 0;
}

extern "C" JNIEXPORT void JNICALL
Java_com_matthew_mboy_MainActivity_setButtonDown(
        JNIEnv* env,
        jobject /* this */,
        jint button,
        jboolean value) {
    gui->setButtonState(button, value);
}
