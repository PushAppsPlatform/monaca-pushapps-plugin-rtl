#import "CDVPushApps.h"
#import "AppDelegate.h"

#define CDVPushApps_DeviceTokenCallbackId @"CDVPushApps_DeviceTokenCallbackId"
#define CDVPushApps_LastPushDictionary @"CDVPushApps_LastPushDictionary"

@interface CDVPushApps() <PushAppsDelegate>

@end

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wincomplete-implementation"

@implementation CDVPushApps

#pragma clang diagnostic pop

- (void)pluginInitialize
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(checkForLaunchOptions:) name:@"UIApplicationDidFinishLaunchingNotification" object:nil];
}

- (void)checkForLaunchOptions:(NSNotification *)notification
{
    NSDictionary *launchOptions = [notification userInfo] ;

    // This code will be called immediately after application:didFinishLaunchingWithOptions:.
    NSDictionary *notifDictionary = [launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
    if (notifDictionary) {
        [[NSUserDefaults standardUserDefaults] setObject:notifDictionary forKey:CDVPushApps_LastPushDictionary];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
}

- (void)onDeviceReady:(CDVInvokedUrlCommand*)command
{
    [PushApps setDelegate:self];

    NSString *sdkKey = [command.arguments objectAtIndex:0];

    if (!sdkKey) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"sdkKey parameter is mandatory"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }

    [PushApps registerDeviceWithSdkKey:sdkKey];

    NSDictionary *checkForLastMessage = [[NSUserDefaults standardUserDefaults] objectForKey:CDVPushApps_LastPushDictionary];
    if (checkForLastMessage) {
        [self updateWithMessageParams:checkForLastMessage];

        // Clear last message
        [[NSUserDefaults standardUserDefaults] removeObjectForKey:CDVPushApps_LastPushDictionary];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
}

- (void)getDeviceId:(CDVInvokedUrlCommand*)command
{
    NSString *deviceId = [PushApps getDeviceId];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:deviceId];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)registerToPushNotifications:(CDVInvokedUrlCommand*)command
{
    [[NSUserDefaults standardUserDefaults] setObject:command.callbackId forKey:CDVPushApps_DeviceTokenCallbackId];
    [[NSUserDefaults standardUserDefaults] synchronize];

    [PushApps registerForPushNotifications:nil];
}

- (void)isPushEnabled:(CDVInvokedUrlCommand*)command
{
    BOOL enabled = [PushApps isRegisterForPushNotifications];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:enabled];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)enablePush:(CDVInvokedUrlCommand*)command
{
    NSInteger enabled = [[command.arguments objectAtIndex:0] integerValue];

    if (enabled == 1) {
        [PushApps enablePushNotifications];
    } else {
        [PushApps disablePushNotifications];
    }
}

- (void)trackUrlView:(CDVInvokedUrlCommand*)command
{
    NSString *url = [command.arguments objectAtIndex:0];
    if ([url length] == 0) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"the url cannot be empty"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    @try {
        [PushApps reportArticleView:url];
    } @catch (NSException *exception) {
        NSLog(@"exception: %@", exception);
    } @finally {

    }

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:nil];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getSdkVersion:(CDVInvokedUrlCommand*)command
{
    NSString *sdkVersion = [PushApps getSdkVersion];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:sdkVersion];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)addTag:(CDVInvokedUrlCommand*)command
{
    NSString *tagName = [command.arguments objectAtIndex:0];
    BOOL enabled = [command.arguments objectAtIndex:1];

    PATag *tag = [[PATag alloc] initWithName:tagName andValue:enabled];
    [PushApps addTag:tag];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)removeTag:(CDVInvokedUrlCommand*)command
{
    NSString *tagName = [command.arguments objectAtIndex:0];
    [PushApps removeTag:tagName];
}

- (void)getTags:(CDVInvokedUrlCommand*)command
{
    [PushApps getTags:^(id object, NSError *error) {

        NSMutableArray *events = [NSMutableArray array];

        for (int i = 0; i < [object count]; ++i) {
            PATag *tag = [object objectAtIndex:i];
            [events addObject:[tag toDictionary]];
        }

        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:events];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

    }];
}

#pragma mark - PushApps

- (void)onDidRegisterToPushNotificationsWithDeviceToken:(NSString *)deviceToken
{
    NSString *callbackId = [[NSUserDefaults standardUserDefaults] objectForKey:CDVPushApps_DeviceTokenCallbackId];
    if (!callbackId) {
        return;
    }

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:deviceToken];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];

    [[NSUserDefaults standardUserDefaults] removeObjectForKey:CDVPushApps_DeviceTokenCallbackId];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

- (void)onDidFailToRegisterToPushNotificationsWithError:(NSError *)error
{
    NSString *callbackId = [[NSUserDefaults standardUserDefaults] objectForKey:CDVPushApps_DeviceTokenCallbackId];
    if (!callbackId) {
        return;
    }

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];

    [[NSUserDefaults standardUserDefaults] removeObjectForKey:CDVPushApps_DeviceTokenCallbackId];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

- (void)onDidReceiveRemoteNotificationWithInfo:(NSDictionary *)userInfo
{
    [self updateWithMessageParams:userInfo];
}

- (void)updateWithMessageParams:(NSDictionary *)pushNotification
{
    // Clear application badge
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];

    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
    NSError *innerJSONparsingError;
    for (id key in pushNotification) {
        if ([[pushNotification objectForKey:key] isKindOfClass:[NSString class]]) {

            NSDictionary *JSON = [NSJSONSerialization JSONObjectWithData: [[pushNotification objectForKey:key] dataUsingEncoding:NSUTF8StringEncoding] options: NSJSONReadingMutableContainers error: &innerJSONparsingError];

            if (JSON) {
                [dictionary setObject:JSON forKey:key];
            }
            else {
                [dictionary setObject:[pushNotification objectForKey:key] forKey:key];
            }

        }
        else {
            [dictionary setObject:[pushNotification objectForKey:key] forKey:key];
        }
    }

    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dictionary options:0 error:&error];

    NSString *jsonString = @"{}";
    if (jsonData) {
        jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    }

    // Update JS
    NSString *javascripCode = [NSString stringWithFormat:@"PushNotification.messageClicked('%@')", jsonString];
    [self.commandDelegate evalJs:javascripCode];
}

@end
