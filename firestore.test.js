const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_EMAIL = "rafiapp372@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
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

test("Unauthenticated user cannot read or write posts, channels, or private settings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("posts").where("isRemoved", "==", false).get());
  await assertFails(unauthDb.collection("channels").where("isBanned", "==", false).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("private").doc("settings").get());
});

test("Channel create, partial update, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("channels").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Studio",
      handle: "@alicestudio",
      bio: "Creator",
      followersCount: 10,
      followingCount: 2,
      isBanned: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Cannot create channel for another user
  await assertFails(
    bobDb.collection("channels").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Hijack",
      handle: "@hijack",
      followersCount: 0,
      followingCount: 0,
      isBanned: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob cannot edit Alice's bio or ban status
  await assertFails(
    bobDb.collection("channels").doc(ALICE_UID).update({
      bio: "Hacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Alice can edit her own bio
  await assertSucceeds(
    aliceDb.collection("channels").doc(ALICE_UID).update({
      bio: "Filmmaker in Dhaka",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Private PII settings are strictly owner-only", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("private").doc("settings").set({
      userId: ALICE_UID,
      email: "alice@example.com",
      language: "bn",
      darkMode: true,
      privacyMode: "PUBLIC",
      twoFactorEnabled: true,
      updatedAt: now,
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("private").doc("settings").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("private").doc("settings").update({
      language: "en",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Posts & Videos: Kotlin-mirrored queries succeed, unfiltered fail, shadow fields rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("posts").doc("post_1").set({
      userId: ALICE_UID,
      authorName: "Alice",
      authorHandle: "@alice",
      caption: "Hello RF TUBE!",
      postType: "TEXT",
      likesCount: 0,
      commentsCount: 0,
      isRemoved: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject ghost field / @DocumentId id leak
  await assertFails(
    aliceDb.collection("posts").doc("post_2").set({
      id: "post_2",
      userId: ALICE_UID,
      authorName: "Alice",
      authorHandle: "@alice",
      caption: "Ghost id",
      postType: "TEXT",
      likesCount: 0,
      commentsCount: 0,
      isRemoved: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Filtered query succeeds, unfiltered fails
  await assertSucceeds(aliceDb.collection("posts").where("isRemoved", "==", false).get());
  await assertFails(aliceDb.collection("posts").get());
});

test("Reports & Admin moderation: non-admin cannot resolve report or ban user; verified admin can", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const adminDb = testEnv.authenticatedContext("admin_uid", {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  const spoofAdminDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("reports").doc("rep_1").set({
      reporterId: ALICE_UID,
      targetType: "POST",
      targetId: "post_1",
      reason: "Spam link",
      status: "OPEN",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(bobDb.collection("reports").doc("rep_1").get());
  await assertFails(spoofAdminDb.collection("reports").doc("rep_1").get());
  await assertFails(
    aliceDb.collection("reports").doc("rep_1").update({
      status: "RESOLVED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    adminDb.collection("reports").doc("rep_1").update({
      status: "RESOLVED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
