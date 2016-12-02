//
//  PushApps.h
//  PushApps
//
//  Created by Asaf Ron on 07/11/2016.
//  Copyright © 2016 PushApps. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "PATag.h"

#define SDK_VERSION @"1.0.9"

//! Project version number for PushApps.
FOUNDATION_EXPORT double PushAppsVersionNumber;

//! Project version string for PushApps.
FOUNDATION_EXPORT const unsigned char PushAppsVersionString[];

@protocol PushAppsDelegate <NSObject>

@optional
- (void)onDidRegisterToPushNotificationsWithDeviceToken:(NSString *)deviceToken;

@optional
- (void)onDidFailToRegisterToPushNotificationsWithError:(NSError *)error;

@optional
- (void)onDidReceiveRemoteNotificationWithInfo:(NSDictionary *)userInfo;

@end

// In this header, you should import all the public headers of your framework using statements like #import <PushApps/PublicHeader.h>

typedef void (^PASimpleCompletionBlock)();

typedef void (^PANotificationContentHandlerBlock)(NSString *articleUrl);

typedef void (^PACompletionBlock)(id object, NSError *error);

typedef void (^registerDeviceCompletion)(NSError *error);

typedef void (^setTokenCompletion)(NSError *error);

typedef void (^clickCompletion)(NSError *error);

typedef void (^eventCompletion)(NSError *error);

typedef void (^getWidgetFeedCompletion)(NSArray *articles, NSError *error);

typedef void (^getTagsCompletion)(NSArray *tags, NSError *error);

typedef void (^getCampaignDetailsCompletion)(NSDictionary *campaignDetails, NSError *error);

@interface PushApps : NSObject

@property (nonatomic, weak) id<PushAppsDelegate> delegate;
+ (void)setDelegate:(id<PushAppsDelegate>)delegate;

// check this flag to see if the Pushapps service is currently available
+ (BOOL)isPushAppsServiceAvailable;

// initialize push notifications for the application
+ (void)registerForPushNotifications:(NSDictionary *)launchOptions;

+ (void)enablePushNotifications;
+ (void)disablePushNotifications;

+ (BOOL)isRegisterForPushNotifications;

+ (NSString *)getDeviceId;

+ (NSString *)getSdkVersion;

// register to the Pushapps service
+ (void)registerDeviceWithSdkKey:(NSString*)sdkKey;

// send APNS device token to the Pushapps service
+ (void)setDevicePushToken:(NSData *)deviceToken;

+ (void)failedToRegisterToPushNotificationsWithError:(NSError *)error;

// set call back block to handle campaign content
+ (void)setNotificationContentHandler:(PANotificationContentHandlerBlock)handler;

+ (void)handleReceivedNotification:(NSDictionary *)userInfo;

// report article view
+ (void)reportArticleView:(NSString *)articleUrl;

+ (void)getWidgetFeedForParams:(NSDictionary *)params withCompletionBlock:(PACompletionBlock)completion;

+ (void)handleOpenUrl:(NSURL *)url withOpenBrowserBlock:(PASimpleCompletionBlock)openBrowserBlock;

+ (void)addTag:(PATag *)tag;

+ (void)addTags:(NSArray *)tags;

+ (void)removeTag:(NSString *)tagName;

+ (void)removeTags:(NSArray *)tags;

+ (void)getTags:(PACompletionBlock)completion;

+ (NSString *)getUrlFromNotificationInfo:(NSDictionary *)userInfo;

@end
