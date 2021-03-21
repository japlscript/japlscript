/*
 * =================================================
 * Copyright 2006-2010 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */

#import "com_tagtraum_japlscript_execution_CocoaScriptExecutor.h"
#import <Cocoa/Cocoa.h>
#import "AppleScriptExecutor.h"

NSString* osTypeToFourCharCode(OSType osType) {
    SInt32 inType = EndianU32_NtoB(osType);
    char code[5];
    memcpy(code,&inType,sizeof(inType));
    code[4] = 0;
    return [NSString stringWithCString:code encoding: NSASCIIStringEncoding];
}

OSType fourCharCodeToOSType(NSString* inCode) {
    OSType rval = 0;
    memcpy(&rval,[inCode cStringUsingEncoding: NSASCIIStringEncoding],sizeof(rval));
    return rval;
}

void typeToString(NSAppleEventDescriptor *descriptor, NSMutableString* buffer) {
    [buffer appendString:[NSString stringWithFormat:@"%Cclass ", (unichar)0x00ab]];
    [buffer appendString:osTypeToFourCharCode([descriptor typeCodeValue])];
    [buffer appendString:[NSString stringWithFormat:@"%C", (unichar)0x00bb]];
}

void dateToString(NSAppleEventDescriptor *descriptor, NSMutableString* buffer) {
    LongDateTime longDateTime;
    OSStatus status;
    CFAbsoluteTime absoluteTime;

    [[descriptor data] getBytes:&longDateTime length:sizeof(longDateTime)];
    status = UCConvertLongDateTimeToCFAbsoluteTime(longDateTime, &absoluteTime);
    if (status == noErr) {
        NSDate *resultDate = (NSDate *)CFDateCreate(NULL, absoluteTime);
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        dateFormatter.locale = [NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"];
        dateFormatter.dateFormat = @"yyyy-MM-dd'T'HH:mm:ssZZZZZ";
        dateFormatter.timeZone = [NSTimeZone timeZoneForSecondsFromGMT:0];
        [buffer appendString:[dateFormatter stringFromDate: resultDate]];
        [dateFormatter release];
        CFRelease(resultDate);
    } else {
        [buffer appendString:@"BROKEN DATE"];
    }
}

void objectToString(NSAppleEventDescriptor *descriptor, NSMutableString* buffer) {
    // we have an object, yeah!
    NSAppleEventDescriptor *form = [descriptor descriptorForKeyword:'form'];
    NSAppleEventDescriptor *want = [descriptor descriptorForKeyword:'want'];
    NSAppleEventDescriptor *seld = [descriptor descriptorForKeyword:'seld'];
    NSAppleEventDescriptor *from = [descriptor descriptorForKeyword:'from'];
    /*
    NSLog(@"== Descriptor ==");
    NSLog(@"form: %@", form);
    NSLog(@"want: %@", want);
    NSLog(@"seld: %@", seld);
    NSLog(@"from: %@", from);
    */
    [buffer appendString:[NSString stringWithFormat:@" %Cclass ", (unichar)0x00ab]];
    [buffer appendString:osTypeToFourCharCode([want typeCodeValue])];
    [buffer appendString:[NSString stringWithFormat:@"%C", (unichar)0x00bb]];
    if ([form typeCodeValue] == 'ID  ') {
        [buffer appendString:@" id"];
    }
    if ([seld descriptorType] == 'utxt') {
        [buffer appendString:@" \""];
        if ([seld stringValue] != nil) [buffer appendString:[seld stringValue]];
        [buffer appendString:@"\""];
    } else {
        [buffer appendString:@" "];
        if ([seld stringValue] != nil) [buffer appendString:[seld stringValue]];
    }
    if ([from descriptorType] == 'obj ') {
        [buffer appendString:@" of"];
        objectToString(from, buffer);
    }
}

void pictToString(NSAppleEventDescriptor *descriptor, NSMutableString *buffer) {
    // we have a picture, yeah!
    /*
     NSAppleEventDescriptor *form = [descriptor descriptorForKeyword:'form'];
     NSAppleEventDescriptor *want = [descriptor descriptorForKeyword:'want'];
     NSAppleEventDescriptor *seld = [descriptor descriptorForKeyword:'seld'];
     NSAppleEventDescriptor *from = [descriptor descriptorForKeyword:'from'];
     NSLog(@"== Descriptor ==");
     NSLog(@"form: %@", form);
     NSLog(@"want: %@", want);
     NSLog(@"seld: %@", seld);
     NSLog(@"from: %@", from);
     NSLog(@"data length: %i", [[descriptor data] length]);
     */
    [buffer appendString:[NSString stringWithFormat:@"%Cdata PICT", (unichar)0x00ab]];
    NSData *data = [descriptor data];
    const int length = [data length];
    const unsigned char *bytes = [data bytes];
    int i;
    for (i=0; i<length; i++) {
        [buffer appendFormat: @"%02x", (unsigned char)bytes[i]];
    }
    [buffer appendString:[NSString stringWithFormat:@"%C", (unichar)0x00bb]];
}

void tdtaToString(NSAppleEventDescriptor *descriptor, NSMutableString *buffer) {
    // we have typed data, yeah!
    /*
     NSAppleEventDescriptor *form = [descriptor descriptorForKeyword:'form'];
     NSAppleEventDescriptor *want = [descriptor descriptorForKeyword:'want'];
     NSAppleEventDescriptor *seld = [descriptor descriptorForKeyword:'seld'];
     NSAppleEventDescriptor *from = [descriptor descriptorForKeyword:'from'];
     NSLog(@"== Descriptor ==");
     NSLog(@"form: %@", form);
     NSLog(@"want: %@", want);
     NSLog(@"seld: %@", seld);
     NSLog(@"from: %@", from);
     NSLog(@"data length: %i", [[descriptor data] length]);
     */
    [buffer appendString:[NSString stringWithFormat:@"%Cdata tdta", (unichar)0x00ab]];
    NSData *data = [descriptor data];
    const int length = [data length];
    const unsigned char *bytes = [data bytes];
    int i;
    for (i=0; i<length; i++) {
        [buffer appendFormat: @"%02x", (unsigned char)bytes[i]];
    }
    [buffer appendString:[NSString stringWithFormat:@"%C", (unichar)0x00bb]];
}

jstring toJString(JNIEnv *env, NSString *nsString) {
    jsize buflength = [nsString length];
    //NSLog(@"string length: %i", buflength);
    
    //unichar buffer[buflength];
    unichar *buffer = malloc(buflength * sizeof(unichar));
    if (buffer == NULL) {
        NSLog(@"ERROR: Failed to allocate jstring buffer of size: %lu", buflength * sizeof(unichar));
    }
    [nsString getCharacters:buffer];
    jstring result = (*env)->NewString(env, (jchar *)buffer, buflength);
    free(buffer);
    return result;
}

jobjectArray toJStringArray(JNIEnv *env, NSArray *nsArray) {
    jobjectArray applicationArgs = (*env)->NewObjectArray(env, [nsArray count],
                                                          (*env)->FindClass(env, "java/lang/String"), NULL);
    int i;
    for (i=0; i<[nsArray count]; i++) {
        (*env)->SetObjectArrayElement(env, applicationArgs, i, toJString(env, (NSString*)[nsArray objectAtIndex:i]));
    } 
    return applicationArgs;
}

JNIEXPORT jstring JNICALL Java_com_tagtraum_japlscript_execution_CocoaScriptExecutor_execute
(JNIEnv *env, jobject this, jstring arg) {
    // Never assume an AutoreleasePool is in place, unless you are on the main AppKit thread
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    jstring javaResult = NULL;
    NSAppleEventDescriptor *result = nil;
    @try {
        // get NSString from jstring
        const jchar *chars = (*env)->GetStringChars(env, arg, NULL);
        NSString *scriptString = [NSString stringWithCharacters:(UniChar *)chars
                                                         length:(*env)->GetStringLength(env, arg)];
        (*env)->ReleaseStringChars(env, arg, chars);    
        
        AppleScriptExecutor *executor = [[AppleScriptExecutor alloc] init];
        [executor performSelectorOnMainThread:@selector(execute:)
                                   withObject:scriptString
                                waitUntilDone:YES];

        result = [executor result];
        if (result == nil) {
            // we have an error. throw an exception with the error dictionary as message
            jclass excCls = (*env)->FindClass(env, "com/tagtraum/japlscript/JaplScriptException");
            if (excCls != NULL) {
                //NSLog(@"ErrorsObject: %@", [executor errors]);
                const char *message = [[NSString stringWithFormat:@"%@", [executor errors]] UTF8String];
                (*env)->ThrowNew(env, excCls, message);
            }
        } else if ([result descriptorType] == 'type' && [result typeCodeValue] == 'msng') {
            // java result is null (msng = missing)
            // do nothing
        } else {

            /*
            NSLog(@"%@", result);
            NSLog(@"Type: %@, StringValue: %@", osTypeToFourCharCode([result descriptorType]), [result stringValue]);
            NSLog(@"NumberOfItems: %u", [result numberOfItems]);

            NSLog(@"EventID: %@", [result eventID]);
            NSLog(@"EventClass: %@", [result eventClass]);
            NSLog(@"Type: %@", osTypeToFourCharCode([result descriptorType]));
            NSLog(@"Code: %@", osTypeToFourCharCode([result typeCodeValue]));
            NSLog(@"Obj : %u", [result descriptorType] == 'obj ');
            */

            NSMutableString *buffer = [[NSMutableString alloc] init];
            
            if ([result descriptorType] == 'obj ') {
                // we have an object
                objectToString(result, buffer);
            }
            else if ([result descriptorType] == 'ldt ') {
                // we have a type
                //NSLog(@"We have a date.");
                dateToString(result, buffer);
            }
            else if ([result descriptorType] == 'type') {
                // we have a type
                //NSLog(@"We have a type.");
                typeToString(result, buffer);
            }
            else if ([result descriptorType] == 'PICT') {
                // we have a picture
                //NSLog(@"We have a picture.");
                pictToString(result, buffer);
            }
            else if ([result descriptorType] == 'tdta') {
                // we have a picture
                //NSLog(@"We have a picture.");
                tdtaToString(result, buffer);
            }
            else if ([result descriptorType] == 'alis') {
                // we have an alis
                //NSLog(@"alis");
                
                OSErr myErr = noErr;
                NSData *data;
                FSRef  fsRef;
                Boolean wasChanged;
                AliasHandle aliasHandle;
                
                data = [result data];
                aliasHandle = (AliasHandle)NewHandle( [data length] );
                // get a real Handle
                if ( NULL != aliasHandle ) {
                    [data getBytes: *aliasHandle];
                    myErr = FSResolveAliasWithMountFlags(NULL, aliasHandle,  
                                                         &fsRef, &wasChanged, kResolveAliasFileNoUI);
                    DisposeHandle( (Handle)aliasHandle );

                    if (myErr == noErr) {
                        CFURLRef resolvedUrl = CFURLCreateFromFSRef(NULL, &fsRef);
                        NSString *resolvedPath = (NSString*) CFURLCopyFileSystemPath( resolvedUrl, kCFURLPOSIXPathStyle);
                        CFRelease(resolvedUrl);
                        //NSLog( @"Path: %@", resolvedPath );
                        // do whatever with resolvedPath
                        [buffer appendString: resolvedPath];
                    } else {
                        // we have an error. throw an exception with the error code and the result dictionary as message
                        jclass excCls = (*env)->FindClass(env, "com/tagtraum/japlscript/JaplScriptException");
                        if (excCls != NULL) {
                            const char *message = [[NSString stringWithFormat:@"Failed to resolve alias. Error=%i, Result=%@", myErr, result] UTF8String];
                            (*env)->ThrowNew(env, excCls, message);
                        }
                    }
                }
            }
            else if ([result descriptorType] == 'list') {
                [buffer appendString:@"{"];
                // we have a list
                long int i;
                for (i=1; i<=[result numberOfItems]; i++)  {
                    NSAppleEventDescriptor *currentResult = [result descriptorAtIndex:i];
                    if ([currentResult descriptorType] == 'obj ') { 
                        objectToString(currentResult, buffer);
                    } else if ([result stringValue] != NULL && [result stringValue] != nil) {
                        // we have a literal or something else...
                        [buffer appendString:[result stringValue]];
                    }
                    if (i<[result numberOfItems]) {
                        [buffer appendString:@","];
                    } 
                }
                [buffer appendString:@"}"];
            }
            else if ([result stringValue] != NULL && [result stringValue] != nil) {
                // we have a literal or something else...
                [buffer appendString:[result stringValue]];
            }
            javaResult = toJString(env, buffer);
            //NSLog(@"Buffer: %@", buffer);
        }
        
    }
    @catch (NSException *exception) {
        // we have an error. throw an exception with the error dictionary as message
        jclass excCls = (*env)->FindClass(env, "com/tagtraum/japlscript/JaplScriptException");
        if (excCls != NULL) {
            const char *message = [[NSString stringWithFormat:@"NSException: %@ (%@, %@)\nScriptResult: %@",
                                    [exception name], [exception reason], [exception userInfo], result] UTF8String];
            (*env)->ThrowNew(env, excCls, message);
        }   
    }
    @finally {
        [pool release];
    }
    return javaResult;
}


