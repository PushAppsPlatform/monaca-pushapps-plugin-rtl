//
//  NotificationService.m
//  NotificationService
//
//  Created by Asaf Ron on 11/11/2016.
//
//

#import "NotificationService.h"

@interface NotificationService ()

@property (nonatomic, strong) void (^contentHandler)(UNNotificationContent *contentToDeliver);
@property (nonatomic, strong) UNMutableNotificationContent *bestAttemptContent;

@end

@implementation NotificationService

- (instancetype)init
{
    self = [super init];
    if (self) {
        [PushApps registerDeviceWithSdkKey:@"380b7f2f-5fd2-4d2e-b72f-265a27bd9347"];
    }
    return self;
}



@end
