//
//  PANotificationServiceExtension.h
//  PushApps
//
//  Created by Asaf Ron on 07/11/2016.
//  Copyright © 2016 PushApps. All rights reserved.
//

#import <UserNotifications/UserNotifications.h>

@interface PANotificationServiceExtension : UNNotificationServiceExtension

@property (nonatomic, strong) NSString *notificationText;
@property (nonatomic, strong) NSString *campaignId;
@property (nonatomic, strong) NSString *contentUrl;
@property (nonatomic, strong) NSString *videoUrl;
@property (nonatomic, strong) NSString *imageUrl;

// Subclass may override this method to extract fields from a non PushApps notification
// and set the properties notificationText, campaignId & contentUrl
- (void)extractCampaignFieldsFromNotification:(NSDictionary *)userInfo;

@end
