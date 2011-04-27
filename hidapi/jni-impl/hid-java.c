#include <assert.h>
#include <jni.h>

#include "hidapi/hidapi.h"
#include "hid-java.h"

void throwIOException(JNIEnv *env)
{
    jclass exceptionClass;
    
    exceptionClass = (*env)->FindClass(env, "java/io/IOException");
    if (exceptionClass == NULL) 
    {
        /* Unable to find the exception class, give up. */
        assert(0);
        return;
    }
    
    (*env)->ThrowNew(env, exceptionClass, ""); //TODO: use hid_error() here
}