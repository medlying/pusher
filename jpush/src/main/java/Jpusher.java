import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jiguang.common.utils.Preconditions;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceType;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import com.sodacar.guice.modules.env.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author bosong
 * @date 2019-09-11
 * @des 极光推送
 */
public class Jpusher implements Pusher, Closeable {

    private static Logger logger = LoggerFactory.getLogger(Jpusher.class);

    private static final JPushClient jPushClient = new JPushClient(secret(), key(), null, ClientConfig.getInstance());

    private static String key() {
        return String.valueOf(Env.group("jpush").prop("key")
                .orElseThrow(() -> new RuntimeException("configuration: jpush key not exist")));
    }

    private static String secret() {
        return String.valueOf(Env.group("jpush").prop("key")
                .orElseThrow(() -> new RuntimeException("configuration: jpush secret not exist")));
    }

    private static PusherResult push(PushPayload pushPayload) {
        try {
            final PushResult pushResult = jPushClient.sendPush(pushPayload);
            logger.info("send push result: {}", pushResult);
            if (pushResult.isResultOK()) {
                logger.info("send push OK!");
            } else {
                logger.warn("send push failed!");
                return new JPusherResult(PusherResult.Code.FAIL, "push fail");
            }
            return new JPusherResult(PusherResult.Code.SUCCESS, "push ok");
        } catch (APIConnectionException e) {
            logger.error("jpush send push connection error", e);
            return new JPusherResult(PusherResult.Code.CONNECTION_ERROR, "connection error");
        } catch (APIRequestException e) {
            logger.error("jpush send push request exception", e);
            return new JPusherResult(PusherResult.Code.REQUEST_FAIL, "request error");
        }
    }

    public static PusherResult pushNotification(List<String> audiences, String title, String content, String cid, Map<String, String> extras) {
        Preconditions.checkArgument(! (content == null), "content must not be null");
        final PushPayload pushPayload = PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.alias(audiences))
                .setNotification(Notification.newBuilder()
                        .setAlert(content)
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setAlert(content)
                                .setTitle(title)
                                .addExtras(extras)
                                .build())
                        .addPlatformNotification(IosNotification.newBuilder()
                                .setAlert(content)
                                .build())
                        .build())
                .setCid(cid)
                .setOptions(Options.newBuilder().setApnsProduction(JPusherMessage.apnsProduction).build())
                .build();
        return push(pushPayload);
    }

    public static PusherResult pushMessage(List<String> audiences, String title, String content, String contentType,
                                           String cid, Map<String, String> extras) {
        Preconditions.checkArgument(! (content == null), "content must not be null");
        final PushPayload pushPayload = PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.alias(audiences))
                .setMessage(Message.newBuilder()
                        .setTitle(title)
                        .setMsgContent(content)
                        .setContentType(contentType)
                        .addExtras(extras)
                        .build())
                .setCid(cid)
                .setOptions(Options.newBuilder().setApnsProduction(JPusherMessage.apnsProduction).build())
                .build();
        return push(pushPayload);
    }

    @Override
    public PusherResult push(PusherMessage pusherMessage) {
        return push((PushPayload) pusherMessage.build());
    }

    @Override
    public void close() {
        if (jPushClient != null) {
            jPushClient.close();
        }
    }
}
