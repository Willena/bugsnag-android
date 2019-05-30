package com.bugsnag.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.bugsnag.android.ndk.NativeBridge;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


@RunWith(AndroidJUnit4.class)
@SmallTest
@SuppressWarnings("unchecked")
public class ObserverInterfaceTest {
    private Configuration config;
    private Client client;
    private BugsnagTestObserver observer;

    /**
     * Configures a new AppData for testing accessors + serialisation
     *
     * @throws Exception if setup failed
     */
    @Before
    public void setUp() throws Exception {
        config = new Configuration("some-api-key");
        config.setDelivery(BugsnagTestUtils.generateDelivery());
        client = new Client(InstrumentationRegistry.getContext(), config);
        client.disableExceptionHandler();
        observer = new BugsnagTestObserver();
        client.addObserver(observer);
    }

    @Test
    public void testUpdateMetadataFromClientSendsMessage() {
        MetaData metadata = new MetaData(new HashMap<String, Object>());
        metadata.addToTab("foo", "bar", "baz");
        client.setMetaData(metadata);
        Object value = findMessageInQueue(
                NativeBridge.MessageType.UPDATE_METADATA, MetaData.class);
        assertEquals(metadata, value);
    }

    @Test
    public void testUpdateMetadataFromConfigSendsMessage() {
        MetaData metadata = new MetaData(new HashMap<String, Object>());
        metadata.addToTab("foo", "bar", "baz");
        client.getConfig().setMetaData(metadata);
        Object value = findMessageInQueue(
                NativeBridge.MessageType.UPDATE_METADATA, MetaData.class);
        assertEquals(metadata, value);
    }

    @Test
    public void testAddMetadataToClientSendsMessage() {
        client.addToTab("foo", "bar", "baz");
        List<Object> metadataItem = (List<Object>)findMessageInQueue(
                NativeBridge.MessageType.ADD_METADATA, List.class);
        assertEquals(3, metadataItem.size());
        assertEquals("foo", metadataItem.get(0));
        assertEquals("bar", metadataItem.get(1));
        assertEquals("baz", metadataItem.get(2));
    }

    @Test
    public void testAddNullMetadataToClientSendsMessage() {
        client.addToTab("foo", "bar", null);
        List<Object> metadataItem = (List<Object>)findMessageInQueue(
                NativeBridge.MessageType.REMOVE_METADATA, List.class);
        assertEquals(2, metadataItem.size());
        assertEquals("foo", metadataItem.get(0));
        assertEquals("bar", metadataItem.get(1));
    }

    @Test
    public void testAddMetadataToMetaDataSendsMessage() {
        client.getMetaData().addToTab("foo", "bar", "baz");
        List<Object> metadataItem = (List<Object>)findMessageInQueue(
                NativeBridge.MessageType.ADD_METADATA, List.class);
        assertEquals(3, metadataItem.size());
        assertEquals("foo", metadataItem.get(0));
        assertEquals("bar", metadataItem.get(1));
        assertEquals("baz", metadataItem.get(2));
    }

    @Test
    public void testClearTabFromClientSendsMessage() {
        client.clearTab("axis");
        Object value = findMessageInQueue(
                NativeBridge.MessageType.CLEAR_METADATA_TAB, String.class);
        assertEquals("axis", value);
    }

    @Test
    public void testClearTabFromMetaDataSendsMessage() {
        client.getMetaData().clearTab("axis");
        Object value =  findMessageInQueue(
                NativeBridge.MessageType.CLEAR_METADATA_TAB, String.class);
        assertEquals("axis", value);
    }

    @Test
    public void testAddNullMetadataToMetaDataSendsMessage() {
        client.getMetaData().addToTab("foo", "bar", null);
        List<Object> metadataItem = (List<Object>)findMessageInQueue(
                NativeBridge.MessageType.REMOVE_METADATA, List.class);
        assertEquals(2, metadataItem.size());
        assertEquals("foo", metadataItem.get(0));
        assertEquals("bar", metadataItem.get(1));
    }

    @Test
    public void testClientSetReleaseStageSendsMessage() {
        client.setReleaseStage("prod-2");
        Object value = findMessageInQueue(
                NativeBridge.MessageType.UPDATE_RELEASE_STAGE, String.class);
        assertEquals("prod-2", value);
    }

    @Test
    public void testConfigSetReleaseStageSendsMessage() {
        client.getConfig().setReleaseStage("prod-2");
        Object value = findMessageInQueue(
                NativeBridge.MessageType.UPDATE_RELEASE_STAGE, String.class);
        assertEquals("prod-2", value);
    }


    @Test
    public void testNotifySendsMessage() {
        client.startSession();
        client.notify(new Exception("ruh roh"));
        Object errorClass = findMessageInQueue(
                NativeBridge.MessageType.NOTIFY_HANDLED, String.class);
        assertEquals("java.lang.Exception", errorClass);
    }

    @Test
    public void testStartSessionSendsMessage() throws InterruptedException {
        client.startSession();
        List<Object> sessionInfo = (List<Object>)findMessageInQueue(
                NativeBridge.MessageType.START_SESSION, List.class);
        assertEquals(4, sessionInfo.size());
        assertTrue(sessionInfo.get(0) instanceof String);
        assertTrue(sessionInfo.get(1) instanceof String);
        assertTrue(sessionInfo.get(2) instanceof Integer);
        assertTrue(sessionInfo.get(3) instanceof Integer);
    }

    @Test
    public void testStopSessionSendsmessage() {
        client.startSession();
        client.stopSession();
        Object msg = findMessageInQueue(NativeBridge.MessageType.STOP_SESSION, null);
        assertNull(msg);
    }

    @Test
    public void testClientSetBuildUUIDSendsMessage() {
        client.setBuildUUID("234423-a");
        Object value = findMessageInQueue(
                NativeBridge.MessageType.UPDATE_BUILD_UUID, String.class);
        assertEquals("234423-a", value);
    }

    @Test
    public void testConfigSetBuildUUIDSendsMessage() {
        client.getConfig().setBuildUUID("234423-a");
        Object value = findMessageInQueue(
                NativeBridge.MessageType.UPDATE_BUILD_UUID, String.class);
        assertEquals("234423-a", value);
    }

    @Test
    public void testClientSetAppVersionSendsMessage() {
        client.setAppVersion("300.0.1x");
        Object value = findMessageInQueue(
                NativeBridge.MessageType.UPDATE_APP_VERSION, String.class);
        assertEquals("300.0.1x", value);
    }

    @Test
    public void testConfigSetAppVersionSendsMessage() {
        client.getConfig().setAppVersion("300.0.1x");
        Object value = findMessageInQueue(
                NativeBridge.MessageType.UPDATE_APP_VERSION, String.class);
        assertEquals("300.0.1x", value);
    }

    @Test
    public void testClientSetContextSendsMessage() {
        client.setContext("Pod Bay");
        String context = (String)findMessageInQueue(
                NativeBridge.MessageType.UPDATE_CONTEXT, String.class);
        assertEquals("Pod Bay", context);
    }

    @Test
    public void testConfigSetContextSendsMessage() {
        client.getConfig().setContext("Pod Bay");
        String context = (String)findMessageInQueue(
                NativeBridge.MessageType.UPDATE_CONTEXT, String.class);
        assertEquals("Pod Bay", context);
    }

    @Test
    public void testClientSetUserId() {
        client.setUserId("personX");
        String value = (String)findMessageInQueue(
                NativeBridge.MessageType.UPDATE_USER_ID, String.class);
        assertEquals("personX", value);
    }

    @Test
    public void testClientSetUserEmail() {
        client.setUserEmail("bip@example.com");
        String value = (String)findMessageInQueue(
                NativeBridge.MessageType.UPDATE_USER_EMAIL, String.class);
        assertEquals("bip@example.com", value);
    }

    @Test
    public void testClientSetUserName() {
        client.setUserName("Loblaw");
        String value = (String)findMessageInQueue(
                NativeBridge.MessageType.UPDATE_USER_NAME, String.class);
        assertEquals("Loblaw", value);
    }

    @Test
    public void testClientClearUserSendsMessage() {
        client.clearUser(); // resets to device ID
        String value = (String)findMessageInQueue(NativeBridge.MessageType.UPDATE_USER_ID,
                                                  String.class);
        assertEquals(client.getDeviceData().getDeviceData().get("id"), value);
        findMessageInQueue(NativeBridge.MessageType.UPDATE_USER_EMAIL, null);
        findMessageInQueue(NativeBridge.MessageType.UPDATE_USER_NAME, null);
    }

    @Test
    public void testLeaveStringBreadcrumbSendsMessage() {
        client.leaveBreadcrumb("Drift 4 units left");
        List<Object> crumb = (List)findMessageInQueue(
                NativeBridge.MessageType.ADD_BREADCRUMB, List.class);
        assertEquals("manual", crumb.get(0));
        assertEquals(BreadcrumbType.MANUAL.toString(), crumb.get(1));

        Map<String, Object> metaData = ((Map)crumb.get(3));
        assertEquals(1, metaData.size());
        assertEquals("Drift 4 units left", metaData.get("message"));
    }

    @Test
    public void testLeaveStringBreadcrumbDirectlySendsMessage() {
        client.breadcrumbs.add(new Breadcrumb("Drift 4 units left"));
        List<Object> crumb = (List)findMessageInQueue(
                NativeBridge.MessageType.ADD_BREADCRUMB, List.class);
        assertEquals("manual", crumb.get(0));
        assertEquals(BreadcrumbType.MANUAL.toString(), crumb.get(1));

        Map<String, Object> metaData = ((Map)crumb.get(3));
        assertEquals(1, metaData.size());
        assertEquals("Drift 4 units left", metaData.get("message"));
    }

    @Test
    public void testClearBreadcrumbsSendsMessage() {
        client.clearBreadcrumbs();
        findMessageInQueue(NativeBridge.MessageType.CLEAR_BREADCRUMBS, null);
    }

    @Test
    public void testClearBreadcrumbsDirectlySendsMessage() {
        client.breadcrumbs.clear();
        findMessageInQueue(NativeBridge.MessageType.CLEAR_BREADCRUMBS, null);
    }

    @Test
    public void testLeaveBreadcrumbSendsMessage() {
        client.leaveBreadcrumb("Rollback", BreadcrumbType.LOG, new HashMap<String, String>());
        List<Object> crumb = (List)findMessageInQueue(
                NativeBridge.MessageType.ADD_BREADCRUMB, List.class);

        assertEquals("Rollback", crumb.get(0));
        assertEquals(BreadcrumbType.LOG.toString(), crumb.get(1));
        assertEquals(0, ((Map)crumb.get(3)).size());
    }

    private Object findMessageInQueue(NativeBridge.MessageType type, Class<?> argClass) {
        for (Object item : observer.observed) {
            if (item instanceof  NativeBridge.Message) {
                NativeBridge.Message message = (NativeBridge.Message)item;
                if (message.type != type) {
                    continue;
                }
                if (argClass == null) {
                    if (((NativeBridge.Message)item).value == null) {
                        return null;
                    }
                } else if (argClass.isInstance(message.value)) {
                    return message.value;
                }
            }
        }
        assertTrue("Failed to find message matching " + type, false);

        return null;
    }

    static class BugsnagTestObserver implements Observer {
        private final ArrayList<Object> observed;

        BugsnagTestObserver() {
            observed = new ArrayList<>(4);
        }

        @Override
        public void update(Observable observable, Object arg) {
            observed.add(arg);
        }
    }
}
