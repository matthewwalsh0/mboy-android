#ifndef MY_APPLICATION_ANDROIDGUI_H
#define MY_APPLICATION_ANDROIDGUI_H

#include <chrono>

#include "Types.h"
#include "GUI.h"
#include "jni.h"
#include "oboe/Oboe.h"
#include "GPU.h"

class AndroidGUI : GUI {
private:
    JNIEnv *env;
    jobject surfaceView;
    std::chrono::steady_clock::time_point lastFrame = std::chrono::steady_clock::now();
    std::chrono::steady_clock::time_point renderTimer = std::chrono::steady_clock::now();
    unsigned short renderFrameCount = 0;
    oboe::ManagedStream managedStream;
    u_int32_t pixels[SCREEN_WIDTH * SCREEN_HEIGHT];
    bool buttonsDown[8] = {[0 ... 7] = false};
public:
    unsigned short fps = 0;
    unsigned short renderFps = 0;
    bool running = false;

    AndroidGUI(JNIEnv *env);
    void setSurface(jobject surfaceView);
    void render(JNIEnv* env);
    void setButtonState(u_int8_t button, bool state);

    void displayBuffer(u_int32_t *pixels) override;
    void displayFPS(u_int16_t fps) override;
    bool isDown(u_int8_t button) override;
    bool isOpen() override;
private:
    void playAudio(float *samples, u_int16_t count) override;
};


#endif //MY_APPLICATION_ANDROIDGUI_H
