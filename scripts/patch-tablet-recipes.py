"""Final, tablet-only adapter. Existing voice and provider lifecycle remains authoritative."""
from pathlib import Path
import shutil

root = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1')
for source in Path('unified/recipes').glob('*.java'):
    shutil.copy2(source, root / source.name)
main = root / 'MainActivity.java'
text = main.read_text(encoding='utf-8')
def once(old, new):
    global text
    if text.count(old) != 1:
        raise SystemExit(f'Recipe adapter expected one anchor: {old!r}')
    text = text.replace(old, new, 1)

once('    private BoopCommandRouter commandRouter;', '''    private BoopCommandRouter commandRouter;
    private final BoopRecipeSession recipeSession = new BoopRecipeSession();
    private BoopRecipePanel recipePanel;
    private long recipeRequestTicket = -1;
''')
once('    private void handleRecognizedSpeech(String transcript) {', '''    private void handleRecognizedSpeech(String transcript) {
        if (handleRecipeSpeech(transcript)) return;''')
once('    public void onBackPressed() {', '''    public void onBackPressed() {
        if (recipeSession.active()) {
            recipeSession.close();
            if (recipePanel != null) recipePanel.close();
            return;
        }''')
once('    protected void onPause() {', '''    protected void onPause() {
        recipeSession.cancelPending();
        if (recipePanel != null) recipePanel.close();''')
once('    protected void onResume() {', '''    protected void onResume() {
        if (recipePanel != null && recipeSession.active()) recipePanel.render(recipeSession);''')
once('    protected void onDestroy() {', '''    protected void onDestroy() {
        recipeSession.close();
        if (recipePanel != null) recipePanel.close();''')
once('    private void handleDeviceSetupFailure(', '''    private boolean handleRecipeSpeech(String transcript) {
        if (getResources().getConfiguration().smallestScreenWidthDp < 600
                || BoopDeviceProfile.resolve(this) != BoopDeviceProfile.Mode.WALL) return false;
        String reply = recipeSession.handle(transcript);
        if (reply == null) return false;
        if (recipePanel == null) recipePanel = new BoopRecipePanel(this, interactionSurface, this::handleRecognizedSpeech);
        recipePanel.render(recipeSession);
        String prompt = recipeSession.prompt();
        if (prompt == null) { speak(reply); return true; }
        final long ticket = recipeSession.ticket();
        if (recipeRequestTicket == ticket) { speak(reply); return true; }
        recipeRequestTicket = ticket;
        final BoopChatMode mode = chatModeStore.load();
        final int revision = chatModeRevision;
        if (mode != BoopChatMode.OPENCODE && mode != BoopChatMode.NATIVE_CHAT) {
            recipeSession.failed("Choose your existing BOOP conversation service in settings to generate recipes. Loaded cooking steps work offline.");
            recipePanel.render(recipeSession); speak(recipeSession.reply()); return true;
        }
        speak(reply);
        executor.execute(() -> {
            CommandOutcome outcome;
            try {
                outcome = mode == BoopChatMode.NATIVE_CHAT ? nativeChatClient.ask(prompt) : generalAssistant.ask(prompt);
            } catch (RuntimeException error) { outcome = CommandOutcome.assistantFailed(); }
            final CommandOutcome result = outcome;
            runOnUiThread(() -> {
                if (!activityInForeground || isFinishing() || isDestroyed()
                        || ticket != recipeSession.ticket()) return;
                if (revision != chatModeRevision) {
                    recipeSession.cancelPending(); recipePanel.render(recipeSession); return;
                }
                if (result.status() != CommandOutcome.Status.ASSISTANT_REPLY) {
                    recipeSession.failed(LocalReply.forOutcome(result));
                } else if (!recipeSession.accept(ticket, result.assistantSpeech())) {
                    recipeSession.failed("I couldn't turn that reply into clear cooking steps. Say that's everything to try again, or close the recipe.");
                }
                recipePanel.render(recipeSession);
                speak(recipeSession.collecting() ? recipeSession.reply()
                        : recipeSession.title() + ". Check the ingredients. Say next step when you're ready.");
            });
        });
        return true;
    }

    private void handleDeviceSetupFailure(''')
main.write_text(text, encoding='utf-8')
