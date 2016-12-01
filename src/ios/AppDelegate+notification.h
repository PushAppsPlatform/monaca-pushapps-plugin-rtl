//
//  AppDelegate+notification.h
//  pushtest
//
//  Created by Robert Easterday on 10/26/12.
//
//

#import "PushApps.h"
#import "AppDelegate.h"

@interface AppDelegate (notification) <PushAppsDelegate>

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
- (void)pushPluginOnApplicationDidBecomeActive:(UIApplication *)application;

@end
