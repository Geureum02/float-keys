package com.example.floatkeys;

interface IPrivilegedInput {
    boolean sendCombination(in int[] keyCodes);
    void destroy();
}
