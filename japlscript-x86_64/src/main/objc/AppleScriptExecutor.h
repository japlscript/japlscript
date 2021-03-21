/*
 * =====================================================
 * Copyright 2006-2010 tagtraum industries incorporated
 * All rights reserved.
 * =====================================================
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
#import <Cocoa/Cocoa.h>


@interface AppleScriptExecutor : NSObject {
	NSAppleEventDescriptor *result;
	NSDictionary *errors;
}

-(NSAppleEventDescriptor *)result;

-(NSDictionary *)errors;

-(void)execute:(NSString *)script;

@end
