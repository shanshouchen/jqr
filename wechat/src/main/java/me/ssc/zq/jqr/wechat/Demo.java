package me.ssc.zq.jqr.wechat;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.xuxiaoxiao.chatapi.wechat.WeChatClient;
import me.xuxiaoxiao.chatapi.wechat.entity.contact.WXContact;
import me.xuxiaoxiao.chatapi.wechat.entity.contact.WXUser;
import me.xuxiaoxiao.chatapi.wechat.entity.message.WXMessage;
import me.xuxiaoxiao.chatapi.wechat.entity.message.WXText;
import me.xuxiaoxiao.chatapi.wechat.entity.message.WXUnknown;
import me.xuxiaoxiao.chatapi.wechat.entity.message.WXVerify;

import java.io.File;
import java.util.Scanner;

public class Demo {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    /**
     * 新建一个模拟微信客户端，并绑定一个简单的监听器
     */
    public static WeChatClient WECHAT_CLIENT = new WeChatClient(new WeChatClient.WeChatListener() {
        @Override
        public void onQRCode(String qrCode) {
            System.out.println("onQRCode：" + qrCode);
        }

        @Override
        public void onLogin() {
            System.out.println(String.format("onLogin：您有%d名好友、活跃微信群%d个", WECHAT_CLIENT.userFriends().size(), WECHAT_CLIENT.userGroups().size()));
        }

        @Override
        public void onMessage(WXMessage message) {
            System.out.println("获取到消息：" + GSON.toJson(message));

            if (message instanceof WXVerify) {
                //是好友请求消息，自动同意好友申请
                WECHAT_CLIENT.passVerify((WXVerify) message);
            } else if (message instanceof WXText && message.fromUser != null && !message.fromUser.id.equals(WECHAT_CLIENT.userMe().id)) {
                //是文字消息，并且发送消息的人不是自己，发送相同内容的消息
                if (message.fromGroup != null) {
                    //群消息
                    WECHAT_CLIENT.sendText(message.fromGroup, message.content);
                } else {
                    //用户消息
                    WECHAT_CLIENT.sendText(message.fromUser, message.content);
                }
            }
        }

        @Override
        public void onContact(WXContact contact, int operate) {
            System.out.println(String.format("检测到联系人变更:%s:%s", operate == WeChatClient.ADD_CONTACT ? "新增" : (operate == WeChatClient.DEL_CONTACT ? "删除" : "修改"), contact.name));
        }
    });

    public static void main(String[] args) {
        //启动模拟微信客户端
        WECHAT_CLIENT.startup();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("请输入指令");
                String s = scanner.nextLine();
                if ("listFriends".equals(s)) {
                    System.out.println("success:" + GSON.toJson(WECHAT_CLIENT.userFriends()));
                } else if ("listGroups".equals(s)) {
                    System.out.println("success:" + GSON.toJson(WECHAT_CLIENT.userGroups()));
                } else if ("sendText".equals(s)) {
                    System.out.println("toContactId:");
                    String toContactId = scanner.nextLine();
                    System.out.println("textContent:");
                    String text = scanner.nextLine();
                    System.out.println("success:" + GSON.toJson(WECHAT_CLIENT.sendText(WECHAT_CLIENT.userContact(toContactId), text)));

                } else if ("sendFile".equals(s)) {
                    System.out.println("toContactId:");
                    String toContactId = scanner.nextLine();
                    System.out.println("filePath:");
                    File file = new File(scanner.nextLine());
                    System.out.println("success:" + GSON.toJson(WECHAT_CLIENT.sendFile(WECHAT_CLIENT.userContact(toContactId), file)));

                } else if ("revokeMsg".equals(s)) {
                    System.out.println("toContactId:");
                    String toContactId = scanner.nextLine();
                    System.out.println("clientMsgId:");
                    String clientMsgId = scanner.nextLine();
                    System.out.println("serverMsgId:");
                    String serverMsgId = scanner.nextLine();
                    WXUnknown wxUnknown = new WXUnknown();
                    wxUnknown.id = Long.valueOf(serverMsgId);
                    wxUnknown.idLocal = Long.valueOf(clientMsgId);
                    wxUnknown.toContact = WECHAT_CLIENT.userContact(toContactId);
                    WECHAT_CLIENT.revokeMsg(wxUnknown);

                } else if ("passVerify".equals(s)) {
                    System.out.println("userId:");
                    String userId = scanner.nextLine();
                    System.out.println("verifyTicket:");
                    String verifyTicket = scanner.nextLine();
                    WXVerify wxVerify = new WXVerify();
                    wxVerify.userId = userId;
                    wxVerify.ticket = verifyTicket;
                    WECHAT_CLIENT.passVerify(wxVerify);

                } else if ("editRemark".equals(s)) {
                    System.out.println("userId:");
                    String userId = scanner.nextLine();
                    System.out.println("remarkName:");
                    String remark = scanner.nextLine();
                    WECHAT_CLIENT.editRemark((WXUser) WECHAT_CLIENT.userContact(userId), remark);

                } else if ("topContact".equals(s)) {
                    System.out.println("contactId:");
                    String contactId = scanner.nextLine();
                    System.out.println("isTop:");
                    String isTop = scanner.nextLine();
                    WECHAT_CLIENT.topContact(WECHAT_CLIENT.userContact(contactId), Boolean.valueOf(isTop.toLowerCase()));

                } else if ("setGroupName".equals(s)) {
                    System.out.println("groupId:");
                    String groupId = scanner.nextLine();
                    System.out.println("name:");
                    String name = scanner.nextLine();
                    WECHAT_CLIENT.setGroupName(WECHAT_CLIENT.userGroup(groupId), name);

                } else if ("quit".equals(s)) {
                    System.out.println("logging out");
                    WECHAT_CLIENT.shutdown();
                    return;
                } else {
                    System.out.println("未知指令");

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
