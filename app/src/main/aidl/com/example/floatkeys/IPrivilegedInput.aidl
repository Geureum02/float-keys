package com.example.floatkeys;

interface IPrivilegedInput {
    boolean sendCombination(in int[] keyCodes);
    boolean keyDown(int keyCode);
    boolean keyUp(int keyCode);
    void destroy();
}
