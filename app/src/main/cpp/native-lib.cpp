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
static Gameboy* gameboy;
static struct config* emulatorConfig;

static void throwJavaException(JNIEnv* env, std::string message) {
    jclass c = env->FindClass("com/matthew/mboy/NativeException");
    env->ThrowNew(c, message.c_str());
}

static void init(JNIEnv *env, jobject surface) {
    env->GetJavaVM(&jvm);
    globalSurface = env->NewGlobalRef(surface);
    emulatorConfig = new struct config();
    gui = new AndroidGUI(env, globalSurface);
}

static void pauseEmulator() {
    gui->running = false;
}

static void continueEmulator(JNIEnv* env) {
    gui->running = true;
    try {
        gameboy->run();
    } catch (std::invalid_argument e) {
        std::string errorMessage = "Bad argument.\n" + std::string(e.what());
        throwJavaException(env, errorMessage);
    }
}

static void stop() {
    gui->running = false;
}

static void render(JNIEnv* env) {
    while(gui->running) {
        gui->render(env);
    }
}

static void start(JNIEnv* env, jstring path) {
    std::string romPath = env->GetStringUTFChars(path, 0);
    try {
        gameboy = new Gameboy(romPath, (GUI*) gui, emulatorConfig);
    } catch (std::invalid_argument e) {
        std::string errorMessage = "Bad argument.\n" + std::string(e.what());
        throwJavaException(env, errorMessage);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_matthew_mboy_Gameboy_nInit(
        JNIEnv* env,
        jobject /* this */,
        jobject surface) {
    init(env, surface);
}

extern "C" JNIEXPORT void JNICALL
Java_com_matthew_mboy_Gameboy_nStart(
        JNIEnv* env,
        jobject /* this */,
        jstring path) {
    start(env, path);
}

extern "C" JNIEXPORT void JNICALL
Java_com_matthew_mboy_Gameboy_nStop(
        JNIEnv* env,
        jobject /* this */) {
    stop();
}

extern "C" JNIEXPORT void JNICALL
Java_com_matthew_mboy_Gameboy_nPause(
        JNIEnv* env,
        jobject /* this */) {
    pauseEmulator();
}

extern "C" JNIEXPORT void JNICALL
Java_com_matthew_mboy_Gameboy_nContinue(
        JNIEnv* env,
        jobject /* this */) {
    continueEmulator(env);
}

extern "C" JNIEXPORT void JNICALL
Java_com_matthew_mboy_Gameboy_nRender(
        JNIEnv* env,
        jobject /* this */) {
    render(env);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_matthew_mboy_Gameboy_nGetFPS(
        JNIEnv* env,
        jobject /* this */) {
    return gui != nullptr ? gui->fps : 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_matthew_mboy_Gameboy_nGetRenderFPS(
        JNIEnv* env,
        jobject /* this */) {
    return gui != nullptr ? gui->renderFps : 0;
}

extern "C" JNIEXPORT void JNICALL
Java_com_matthew_mboy_Gameboy_nSetButtonDown(
        JNIEnv* env,
        jobject /* this */,
        jint button,
        jboolean value) {
    gui->setButtonState(button, value);
}

extern "C" JNIEXPORT void JNICALL
Java_com_matthew_mboy_Gameboy_setTurbo(
        JNIEnv* env,
        jobject /* this */,
        jboolean value) {
    emulatorConfig->turbo = value;
}
