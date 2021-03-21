/*
 * =====================================================
 * Copyright 2006-2010 tagtraum industries incorporated
 * All rights reserved.
 * =====================================================
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
#import "AppleScriptExecutor.h"


@implementation AppleScriptExecutor

-(id)init {
	self = [super init];
	return self;
}

-(NSDictionary *)errors {
	return [[errors retain] autorelease];
}

- (void)setErrors:(NSDictionary *)newErrors {
    if (newErrors != errors) {
        [errors release];
        errors = [newErrors copy];
    }
}

-(NSAppleEventDescriptor *)result {
	return [[result retain] autorelease];
}

- (void)setResult:(NSAppleEventDescriptor *)newResult {
    if (newResult != result) {
        [result release];
        result = [newResult copy];
    }
}

-(void)execute:(NSString *)scriptString {
	NSDictionary* e = [NSDictionary dictionary];
    NSAppleScript *script = [[NSAppleScript alloc] initWithSource:scriptString];
	// check error object...
	[self setResult:[script executeAndReturnError:&e]];
	[self setErrors:e];
    [script release];
}

- (void)dealloc {
    if (result != NULL) {
		[result release];
	}
    if (errors != NULL) {
		[errors release];
	}
    [super dealloc];
}

@end
