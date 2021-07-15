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
#import "japlscriptjnilib.h"

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

void typeToString(JNIEnv *env, NSAppleEventDescriptor *descriptor, NSMutableString* buffer) {
    if ([descriptor typeCodeValue] == 'msng') {
        [buffer appendString:@"missing value"];
    } else {
        [buffer appendString:[NSString stringWithFormat:@"%Cclass ", (unichar)0x00ab]];
        [buffer appendString:osTypeToFourCharCode([descriptor typeCodeValue])];
        [buffer appendString:[NSString stringWithFormat:@"%C", (unichar)0x00bb]];
    }
}

void dateToString(JNIEnv *env, NSAppleEventDescriptor *descriptor, NSMutableString* buffer) {
    LongDateTime longDateTime;
    OSStatus status;
    CFAbsoluteTime absoluteTime;

    [[descriptor data] getBytes:&longDateTime length:sizeof(longDateTime)];
    status = UCConvertLongDateTimeToCFAbsoluteTime(longDateTime, &absoluteTime);
    if (status == noErr) {
        NSDate *descriptorDate = (NSDate *)CFDateCreate(NULL, absoluteTime);
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        dateFormatter.locale = [NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"];
        dateFormatter.dateFormat = @"yyyy-MM-dd'T'HH:mm:ssZZZZZ";
        dateFormatter.timeZone = [NSTimeZone timeZoneForSecondsFromGMT:0];
        [buffer appendString:[dateFormatter stringFromDate: descriptorDate]];
        [dateFormatter release];
        CFRelease(descriptorDate);
    } else {
        [buffer appendString:@"BROKEN DATE"];
    }
}

void objectToString(JNIEnv *env, NSAppleEventDescriptor *descriptor, NSMutableString* buffer) {
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
        objectToString(env, from, buffer);
    }
}

void listToString(JNIEnv *env, NSAppleEventDescriptor *descriptor, NSMutableString* buffer) {
    [buffer appendString:@"{"];
    // we have a list
    long int i;
    for (i=1; i<=[descriptor numberOfItems]; i++)  {
        NSAppleEventDescriptor *currentResult = [descriptor descriptorAtIndex:i];
        appendDescriptor(env, currentResult, buffer);
        if (i<[descriptor numberOfItems]) {
            [buffer appendString:@","];
        }
    }
    [buffer appendString:@"}"];
}

void propertiesToString(JNIEnv *env, NSAppleEventDescriptor *descriptor, NSMutableString* buffer) {
    [buffer appendString:@"{"];
    NSLog(@"Type: %@", osTypeToFourCharCode([descriptor descriptorType]));
    NSLog(@"Code: %@", osTypeToFourCharCode([descriptor typeCodeValue]));
    [buffer appendString:[NSString stringWithFormat:@"%Cproperty pcls%C: %Cclass ",(unichar)0x00ab, (unichar)0x00bb, (unichar)0x00ab]];
    [buffer appendString:osTypeToFourCharCode([descriptor descriptorType])];
    [buffer appendString:[NSString stringWithFormat:@"%C, ", (unichar)0x00bb]];

    long int i;
    for (i=1; i<=[descriptor numberOfItems]; i++)  {
        NSAppleEventDescriptor *currentResult = [descriptor descriptorAtIndex:i];
        AEKeyword keyword = [descriptor keywordForDescriptorAtIndex:i];
        NSAppleEventDescriptor *form = [currentResult descriptorForKeyword:'form'];
        NSAppleEventDescriptor *want = [currentResult descriptorForKeyword:'want'];
        NSAppleEventDescriptor *seld = [currentResult descriptorForKeyword:'seld'];
        NSAppleEventDescriptor *from = [currentResult descriptorForKeyword:'from'];
        NSLog(@"== Descriptor ==");
        NSLog(@"Keyword: %@", osTypeToFourCharCode(keyword));
        NSLog(@"form: %@", form);
        NSLog(@"want: %@", want);
        NSLog(@"seld: %@", seld);
        NSLog(@"from: %@", from);
        NSLog(@"Type: %@", osTypeToFourCharCode([currentResult descriptorType]));
        NSLog(@"Code: %@", osTypeToFourCharCode([currentResult typeCodeValue]));

        [buffer appendString:[NSString stringWithFormat:@"%Cproperty ", (unichar)0x00ab]];
        [buffer appendString:osTypeToFourCharCode(keyword)];
        [buffer appendString:[NSString stringWithFormat:@"%C: ", (unichar)0x00bb]];

        if ([currentResult descriptorType] == 'reco') {
            NSAppleEventDescriptor *usrf = [currentResult descriptorForKeyword:'usrf'];
            usrfToString(env, usrf, buffer);
        } else if ([currentResult descriptorType] == 'utxt') {
            [buffer appendString:@"\""];
            [buffer appendString:[currentResult stringValue]];
            [buffer appendString:@"\""];
        } else {
            appendDescriptor(env, currentResult, buffer);
        }
        if (i<[descriptor numberOfItems]) {
            [buffer appendString:@", "];
        }
    }
    [buffer appendString:@"}"];
}

void usrfToString(JNIEnv *env, NSAppleEventDescriptor *descriptor, NSMutableString* buffer) {
    [buffer appendString:@"{"];
    NSLog(@"Type: %@", osTypeToFourCharCode([descriptor descriptorType]));
    NSLog(@"Code: %@", osTypeToFourCharCode([descriptor typeCodeValue]));

    long int i;
    BOOL key;
    for (i=1; i<=[descriptor numberOfItems]; i++)  {
        key = (i % 2) != 0;
        NSAppleEventDescriptor *currentResult = [descriptor descriptorAtIndex:i];
        AEKeyword keyword = [descriptor keywordForDescriptorAtIndex:i];
        NSAppleEventDescriptor *form = [currentResult descriptorForKeyword:'form'];
        NSAppleEventDescriptor *want = [currentResult descriptorForKeyword:'want'];
        NSAppleEventDescriptor *seld = [currentResult descriptorForKeyword:'seld'];
        NSAppleEventDescriptor *from = [currentResult descriptorForKeyword:'from'];
        NSLog(@"== Descriptor ==");
        NSLog(@"Keyword: %@", osTypeToFourCharCode(keyword));
        NSLog(@"form: %@", form);
        NSLog(@"want: %@", want);
        NSLog(@"seld: %@", seld);
        NSLog(@"from: %@", from);
        NSLog(@"Type: %@", osTypeToFourCharCode([currentResult descriptorType]));
        NSLog(@"Code: %@", osTypeToFourCharCode([currentResult typeCodeValue]));

        if (key && [currentResult descriptorType] == 'utxt') {
            [buffer appendString:[currentResult stringValue]];
            [buffer appendString:@": "];
        } else if ([currentResult descriptorType] == 'utxt') {
            [buffer appendString:@"\""];
            [buffer appendString:[currentResult stringValue]];
            [buffer appendString:@"\""];
        } else {
            appendDescriptor(env, currentResult, buffer);
        }
        if (i<[descriptor numberOfItems] && !key) {
            [buffer appendString:@", "];
        }
    }
    [buffer appendString:@"}"];
}

void pictToString(JNIEnv *env, NSAppleEventDescriptor *descriptor, NSMutableString *buffer) {
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

void tdtaToString(JNIEnv *env, NSAppleEventDescriptor *descriptor, NSMutableString *buffer) {
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

void alisToString(JNIEnv *env, NSAppleEventDescriptor *descriptor, NSMutableString *buffer) {
    // we have an alias
    //NSLog(@"alias");

    OSErr myErr = noErr;
    NSData *data;
    FSRef  fsRef;
    Boolean wasChanged;
    AliasHandle aliasHandle;

    data = [descriptor data];
    aliasHandle = (AliasHandle)NewHandle( [data length] );
    // get a real Handle
    if (NULL != aliasHandle) {
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
            // we have an error. throw an exception with the error code and the descriptor dictionary as message
            jclass excCls = (*env)->FindClass(env, "com/tagtraum/japlscript/JaplScriptException");
            if (excCls != NULL) {
                const char *message = [[NSString stringWithFormat:@"Failed to resolve alias. Error=%i, Result=%@", myErr, descriptor] UTF8String];
                (*env)->ThrowNew(env, excCls, message);
            }
        }
    }
}

void appendDescriptor(JNIEnv *env, NSAppleEventDescriptor *descriptor, NSMutableString *buffer) {
    NSLog(@"%@", descriptor);
    NSLog(@"Type: %@, StringValue: %@", osTypeToFourCharCode([descriptor descriptorType]), [descriptor stringValue]);
    NSLog(@"NumberOfItems: %u", [descriptor numberOfItems]);

    NSLog(@"EventID: %@", [descriptor eventID]);
    NSLog(@"EventClass: %@", [descriptor eventClass]);
    NSLog(@"Type: %@", osTypeToFourCharCode([descriptor descriptorType]));
    NSLog(@"Code: %@", osTypeToFourCharCode([descriptor typeCodeValue]));
    NSLog(@"Obj : %u", [descriptor descriptorType] == 'obj ');

    NSAppleEventDescriptor *form = [descriptor descriptorForKeyword:'form'];
    NSAppleEventDescriptor *want = [descriptor descriptorForKeyword:'want'];
    NSAppleEventDescriptor *seld = [descriptor descriptorForKeyword:'seld'];
    NSAppleEventDescriptor *from = [descriptor descriptorForKeyword:'from'];
    NSLog(@"== Descriptor ==");
    NSLog(@"form: %@", form);
    NSLog(@"want: %@", want);
    NSLog(@"seld: %@", seld);
    NSLog(@"from: %@", from);

    if ([descriptor descriptorType] == 'obj ') {
        // we have an object
        objectToString(env, descriptor, buffer);
    }
    else if ([descriptor descriptorType] == 'ldt ') {
        // we have a date
        //NSLog(@"We have a date.");
        dateToString(env, descriptor, buffer);
    }
    else if ([descriptor descriptorType] == 'type') {
        // we have a type
        //NSLog(@"We have a type.");
        typeToString(env, descriptor, buffer);
    }
    else if ([descriptor descriptorType] == 'PICT') {
        // we have a picture
        //NSLog(@"We have a picture.");
        pictToString(env, descriptor, buffer);
    }
    else if ([descriptor descriptorType] == 'tdta') {
        // we have typed data
        //NSLog(@"We have typed data.");
        tdtaToString(env, descriptor, buffer);
    }
    else if ([descriptor descriptorType] == 'alis') {
        alisToString(env, descriptor, buffer);
    }
    else if ([descriptor descriptorType] == 'list') {
        listToString(env, descriptor, buffer);
    }
    else if ([descriptor descriptorType] == 'reco') {
        NSAppleEventDescriptor *usrf = [descriptor descriptorForKeyword:'usrf'];
        usrfToString(env, usrf, buffer);
    }
    else if ([descriptor stringValue] != NULL && [descriptor stringValue] != nil) {
        // we have a literal or something else...
        [buffer appendString:[descriptor stringValue]];
    } else if ([descriptor descriptorType] != 'null') {
        NSLog(@"%@", descriptor);
        NSLog(@"Type: %@, StringValue: %@", osTypeToFourCharCode([descriptor descriptorType]), [descriptor stringValue]);
        // we simply assume we have all properties of some instance
        // and want to output a record
        propertiesToString(env, descriptor, buffer);
    }
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
    jstring descriptor = (*env)->NewString(env, (jchar *)buffer, buflength);
    free(buffer);
    return descriptor;
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
    NSAppleEventDescriptor *descriptor = nil;
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

        descriptor = [executor result];
        if (descriptor == nil) {
            // we have an error. throw an exception with the error dictionary as message
            jclass excCls = (*env)->FindClass(env, "com/tagtraum/japlscript/JaplScriptException");
            if (excCls != NULL) {
                //NSLog(@"ErrorsObject: %@", [executor errors]);
                const char *message = [[NSString stringWithFormat:@"%@", [executor errors]] UTF8String];
                (*env)->ThrowNew(env, excCls, message);
            }
        } else {
            NSMutableString *buffer = [[NSMutableString alloc] init];
            appendDescriptor(env, descriptor, buffer);
            javaResult = toJString(env, buffer);
            //NSLog(@"Buffer: %@", buffer);
        }
    }
    @catch (NSException *exception) {
        // we have an error. throw an exception with the error dictionary as message
        jclass excCls = (*env)->FindClass(env, "com/tagtraum/japlscript/JaplScriptException");
        if (excCls != NULL) {
            const char *message = [[NSString stringWithFormat:@"NSException: %@ (%@, %@)\nScriptResult: %@",
                                    [exception name], [exception reason], [exception userInfo], descriptor] UTF8String];
            (*env)->ThrowNew(env, excCls, message);
        }   
    }
    @finally {
        [pool release];
    }
    return javaResult;
}
