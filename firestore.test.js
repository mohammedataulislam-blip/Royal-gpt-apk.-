const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_101";
const BOB_UID = "bob_uid_202";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validUserProfilePayload(uid, overrides = {}) {
  const now = new Date(Date.now() - 1000);
  return {
    userId: uid,
    displayName: "Royal Member",
    email: "member@example.com",
    emailVerified: true,
    authProvider: "google",
    darkThemeEnabled: true,
    notificationsEnabled: true,
    saveHistoryEnabled: true,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validConversationPayload(uid, overrides = {}) {
  const now = new Date(Date.now() - 1000);
  return {
    userId: uid,
    title: "Architecture Discussion",
    category: "CODING",
    lastMessagePreview: "Let us design a modular MVVM layer",
    messageCount: 0,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validMessagePayload(uid, convId, overrides = {}) {
  return {
    userId: uid,
    conversationId: convId,
    role: "USER",
    content: "Draft a clean Kotlin Compose repository",
    createdAt: new Date(Date.now() - 1000),
    ...overrides,
  };
}

test("Unauthenticated users cannot read or write profiles, conversations, or messages", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validUserProfilePayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("conversations").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("conversations")
      .doc("conv_1")
      .collection("messages")
      .get()
  );
});

test("UserProfile: owner can create, read, and partially update; cross-user and list scraping denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Owner create succeeds (matches UserProfile.toCreateMap())
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validUserProfilePayload(ALICE_UID))
  );

  // Owner get succeeds
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Partial update of displayName & preferences succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Sovereign Architect",
      darkThemeEnabled: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // List scraping on /users is always denied
  await assertFails(aliceDb.collection("users").get());

  // Cross-user read and write denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({ displayName: "Hijacked" })
  );

  // Reject ghost @DocumentId `id` field & immutable createdAt change
  await assertFails(
    bobDb
      .collection("users")
      .doc(BOB_UID)
      .set(validUserProfilePayload(BOB_UID, { id: BOB_UID }))
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 100),
      updatedAt: new Date(Date.now() - 100),
    })
  );
});

test("Conversations & Messages: Kotlin-mirrored create, query, update, and negative security bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Cannot create message without existing parent conversation
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("conversations")
      .doc("missing_conv")
      .collection("messages")
      .doc("msg_1")
      .set(validMessagePayload(ALICE_UID, "missing_conv"))
  );

  // Owner creates conversation
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("conversations")
      .doc("conv_1")
      .set(validConversationPayload(ALICE_UID))
  );

  // Owner creates message with optional attachmentLabel
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("conversations")
      .doc("conv_1")
      .collection("messages")
      .doc("msg_1")
      .set(validMessagePayload(ALICE_UID, "conv_1", { attachmentLabel: "photo_01.jpg" }))
  );

  // Owner updates conversation preview & count
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("conversations")
      .doc("conv_1")
      .update({
        lastMessagePreview: "Draft a clean Kotlin Compose repository",
        messageCount: 1,
        updatedAt: new Date(Date.now() - 500),
      })
  );

  // Owner list queries matching ConversationRepository orderBy succeed
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("conversations")
      .orderBy("updatedAt", "desc")
      .get()
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("conversations")
      .doc("conv_1")
      .collection("messages")
      .orderBy("createdAt", "asc")
      .get()
  );

  // Reject future timestamp, invalid category, and immutable message update
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("conversations")
      .doc("conv_future")
      .set(
        validConversationPayload(ALICE_UID, {
          createdAt: new Date(Date.now() + 60000),
          updatedAt: new Date(Date.now() + 60000),
        })
      )
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("conversations")
      .doc("conv_bad_cat")
      .set(validConversationPayload(ALICE_UID, { category: "UNKNOWN_TOOL" }))
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("conversations")
      .doc("conv_1")
      .collection("messages")
      .doc("msg_1")
      .update({ content: "Tampered" })
  );

  // Cross-user isolation on conversations and messages
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("conversations").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("conversations")
      .doc("conv_1")
      .collection("messages")
      .doc("msg_1")
      .get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("conversations")
      .doc("conv_1")
      .delete()
  );
});
