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

function buildValidPrefs(uid, serverTimestamp) {
  return {
    userId: uid,
    preferredLanguage: "en",
    themeMode: "dark",
    reducedMotion: false,
    arabicFontScale: 1.0,
    autoplayNext: true,
    defaultRepeatMode: "off",
    ambientSoundId: "none",
    ambientVolume: 0.35,
    favoriteReciterIds: ["7"],
    favoriteTrackIds: ["7_1"],
    savedCollectionIds: ["col_focus"],
    isDemoPremium: false,
    createdAt: serverTimestamp,
    updatedAt: serverTimestamp,
  };
}

test("1. Unauthenticated user cannot read user preferences", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("2. Unauthenticated user cannot list playlists", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("playlists").get()
  );
});

test("3. Authenticated owner can create and read their own preferences", async () => {
  const aliceCtx = testEnv.authenticatedContext(ALICE_UID);
  const aliceDb = aliceCtx.firestore();
  const ts = new Date(Date.now() - 1000);
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(buildValidPrefs(ALICE_UID, ts))
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("4. Cross-user isolation: Alice cannot read or write Bob's preferences", async () => {
  const ts = new Date(Date.now() - 1000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(BOB_UID).set(buildValidPrefs(BOB_UID, ts));
  });
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).update({ preferredLanguage: "ar" }));
});

test("5. Shadow Update attack: Ghost field isAdmin/isVerified rejected on create & update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = new Date(Date.now() - 1000);
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...buildValidPrefs(ALICE_UID, ts),
      isAdmin: true,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(buildValidPrefs(ALICE_UID, ts))
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isVerified: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("6. Identity spoofing: Alice cannot set userId to Bob inside her own playlist", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = new Date(Date.now() - 1000);
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("playlists").doc("pl_1").set({
      userId: BOB_UID,
      name: "Spoofed",
      description: "",
      trackIds: [],
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("7. Playlist CRUD + Client Query Alignment succeeds for owner with userId filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = new Date(Date.now() - 1000);
  const plRef = aliceDb.collection("users").doc(ALICE_UID).collection("playlists").doc("pl_morning");
  await assertSucceeds(
    plRef.set({
      userId: ALICE_UID,
      name: "Morning Reflection",
      description: "Calm morning surahs",
      trackIds: ["7_1", "7_36"],
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("playlists")
      .where("userId", "==", ALICE_UID)
      .get()
  );
  await assertSucceeds(
    plRef.update({
      name: "Morning & Dawn",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("8. Cross-user playlist query denied", async () => {
  const ts = new Date(Date.now() - 1000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("playlists")
      .doc("bob_pl")
      .set({
        userId: BOB_UID,
        name: "Private",
        description: "",
        trackIds: [],
        createdAt: ts,
        updatedAt: ts,
      });
  });
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("playlists")
      .where("userId", "==", BOB_UID)
      .get()
  );
});

test("9. Value poisoning: Oversized playlist name or invalid surahNumber rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = new Date(Date.now() - 1000);
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("playlists").doc("pl_bad").set({
      userId: ALICE_UID,
      name: "x".repeat(200),
      description: "",
      trackIds: [],
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("listening_events").doc("ev_bad").set({
      userId: ALICE_UID,
      trackId: "7_1",
      surahNumber: 999,
      surahName: "Invalid",
      reciterId: "7",
      reciterName: "Alafasy",
      durationListenedSec: 30,
      lastPositionMs: 30000,
      sessionId: "sess_1",
      createdAt: ts,
    })
  );
});

test("10. ListeningEvent append-only immutability: create succeeds, update fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = new Date(Date.now() - 1000);
  const evRef = aliceDb.collection("users").doc(ALICE_UID).collection("listening_events").doc("ev_1");
  await assertSucceeds(
    evRef.set({
      userId: ALICE_UID,
      trackId: "7_1",
      surahNumber: 1,
      surahName: "Al-Fatihah",
      reciterId: "7",
      reciterName: "Mishary Rashid Alafasy",
      durationListenedSec: 45,
      lastPositionMs: 45000,
      sessionId: "sess_1",
      createdAt: ts,
    })
  );
  await assertFails(evRef.update({ durationListenedSec: 120 }));
});

test("11. All catalog ambientSoundId values and preferredAudioSourceCode succeed; invalid values rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = new Date(Date.now() - 1000);
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...buildValidPrefs(ALICE_UID, ts),
      ambientSoundId: "rain_window",
      preferredAudioSourceCode: "alquran_cloud",
    })
  );

  for (const soundId of ["ocean", "forest", "night", "stream", "wind", "rain", "none"]) {
    await assertSucceeds(
      aliceDb.collection("users").doc(ALICE_UID).update({
        ambientSoundId: soundId,
        preferredAudioSourceCode: "everyayah",
        updatedAt: new Date(Date.now() - 400),
      })
    );
  }

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      ambientSoundId: "thunderstorm_unlisted",
      updatedAt: new Date(Date.now() - 200),
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      preferredAudioSourceCode: "unverified_cdn",
      updatedAt: new Date(Date.now() - 200),
    })
  );
});

