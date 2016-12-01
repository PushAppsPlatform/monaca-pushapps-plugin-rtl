//
//  PATag.h
//  PushApps
//
//  Created by Asaf Ron on 07/11/2016.
//  Copyright © 2016 PushApps. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface PATag : NSObject

- (instancetype)initWithName:(NSString *)name andValue:(BOOL)value;

- (NSString *)getName;
- (id)getValue;
- (NSString *)getType;

- (NSDictionary *)toDictionary;

@end
