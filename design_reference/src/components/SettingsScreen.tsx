import React, { useState } from 'react';
import {
  Globe,
  Smartphone,
  ShieldCheck,
  ShieldAlert,
  RefreshCw,
  Download,
  CheckCircle2,
  AlertTriangle,
  KeyRound,
  BookOpen,
  FileCode2,
  Check,
  Palette,
  X,
  ChevronRight,
} from 'lucide-react';
import { AppSettings } from '../types/campaign';
import { Translations, Language } from '../i18n/translations';
import { APP_THEME_COLORS, getThemeColorById } from '../utils/campaignLogic';

interface SettingsScreenProps {
  settings: AppSettings;
  onUpdateSettings: (next: AppSettings) => void;
  currentLanguage: Language;
  onToggleLanguage: () => void;
  isDeviceFrame: boolean;
  onToggleDeviceFrame: () => void;
  onOpenOnboarding: () => void;
  onOpenHandoffSpec: () => void;
  onTriggerToast: (type: 'success' | 'info' | 'error', msg: string) => void;
  t: Translations;
}

type UpdateState =
  | 'idle'
  | 'checking'
  | 'available'
  | 'downloading'
  | 'verifying'
  | 'ready_to_install'
  | 'up_to_date'
  | 'error';

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  settings,
  onUpdateSettings,
  currentLanguage,
  onToggleLanguage,
  isDeviceFrame,
  onToggleDeviceFrame,
  onOpenOnboarding,
  onOpenHandoffSpec,
  onTriggerToast,
  t,
}) => {
  const isAr = currentLanguage === 'ar';

  // 11.11 Application Updates state machine
  const [updateState, setUpdateState] = useState<UpdateState>('idle');
  const [downloadProgress, setDownloadProgress] = useState<number>(0);
  const [showInstallGuidance, setShowInstallGuidance] = useState<boolean>(false);
  const [showBatteryGuidance, setShowBatteryGuidance] = useState<boolean>(false);
  const [isColorModalOpen, setIsColorModalOpen] = useState<boolean>(false);

  const updateField = <K extends keyof AppSettings>(key: K, value: AppSettings[K]) => {
    onUpdateSettings({
      ...settings,
      [key]: value,
    });
  };

  const togglePermission = (permKey: keyof AppSettings['permissions']) => {
    onUpdateSettings({
      ...settings,
      permissions: {
        ...settings.permissions,
        [permKey]: !settings.permissions[permKey],
      },
    });
  };

  // Simulate Check for Updates (11.11)
  const handleCheckForUpdates = () => {
    setUpdateState('checking');
    setTimeout(() => {
      setUpdateState('available');
    }, 600);
  };

  // Simulate Download & Verify Update (11.11)
  const handleDownloadUpdate = () => {
    setUpdateState('downloading');
    setDownloadProgress(25);
    setTimeout(() => setDownloadProgress(65), 350);
    setTimeout(() => {
      setDownloadProgress(100);
      setUpdateState('verifying');
    }, 700);
    setTimeout(() => {
      setUpdateState('ready_to_install');
    }, 1100);
  };

  // Simulate License Verification (11.10)
  const handleVerifyLicense = () => {
    if (!settings.activationCode.trim()) {
      updateField('licenseStatus', 'error');
      return;
    }
    updateField('licenseStatus', 'checking');
    setTimeout(() => {
      updateField('licenseStatus', 'activated');
      onTriggerToast('success', 'Device activation verified.');
    }, 500);
  };

  const isSameCallingHour =
    settings.restrictCallingHours &&
    settings.allowedHourFrom === settings.allowedHourTo;

  const activeThemeColor = getThemeColorById(settings.appColor);

  return (
    <div className="flex-1 overflow-y-auto bg-transparent flex flex-col pb-28">
      {/* Header */}
      <header className="px-[20px] pt-4 pb-3 bg-transparent">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0">
            <h1 className="text-[24px] font-extrabold tracking-tight text-[#111318] leading-tight truncate">
              {t.navSettings}
            </h1>
            <p className="text-[12px] text-[#8A909E] mt-1 font-medium truncate">
              {isAr
                ? 'إعدادات التوقيت، سلوك الحملات، الصلاحيات، والتحديثات'
                : 'Calling timing, campaign behavior, permissions & updates'}
            </p>
          </div>
          <div className="flex items-center gap-2 shrink-0 pt-0.5">
            <button
              type="button"
              onClick={onOpenOnboarding}
              className="min-h-[44px] px-3.5 rounded-full bg-white/60 backdrop-blur-md shadow-[0_6px_20px_-4px_rgba(17,19,24,0.06)] text-[12px] font-bold text-[#111318] hover:bg-white flex items-center gap-1.5 transition-all"
              title="Open 3-step welcome guide"
            >
              <BookOpen className="w-3.5 h-3.5 text-[#111318] stroke-[1.75]" />
              <span>{isAr ? 'الدليل' : 'Guide'}</span>
            </button>
            <button
              onClick={onToggleLanguage}
              className="w-11 h-11 rounded-full bg-white/60 backdrop-blur-md shadow-[0_6px_20px_-4px_rgba(17,19,24,0.06)] flex items-center justify-center text-[#111318] hover:bg-white transition-all"
              aria-label="Toggle language"
            >
              <Globe className="w-4 h-4 stroke-[1.75]" />
            </button>
          </div>
        </div>
      </header>

      <div className="px-[20px] space-y-3.5 flex-1">
        {/* 11.1 General */}
        <section className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] space-y-3.5">
          <h2 className="text-[13.5px] font-bold text-[#111318] tracking-tight">
            {isAr ? '١. الإعدادات العامة' : 'General'}
          </h2>

          {/* Language */}
          <div className="flex items-center justify-between gap-3">
            <span className="text-[13px] text-[#111318] font-semibold">
              {isAr ? 'اللغة' : 'Language'}
            </span>
            <div className="flex items-center gap-1 p-1 bg-[#111318]/[0.05] rounded-full">
              <button
                type="button"
                onClick={() => currentLanguage !== 'en' && onToggleLanguage()}
                className={`px-3.5 py-1 text-[12px] font-semibold rounded-full transition-all ${
                  currentLanguage === 'en'
                    ? 'bg-[#111318] text-white shadow-xs'
                    : 'text-[#8A909E]'
                }`}
              >
                English
              </button>
              <button
                type="button"
                onClick={() => currentLanguage !== 'ar' && onToggleLanguage()}
                className={`px-3.5 py-1 text-[12px] font-semibold rounded-full transition-all ${
                  currentLanguage === 'ar'
                    ? 'bg-[#111318] text-white shadow-xs'
                    : 'text-[#8A909E]'
                }`}
              >
                العربية
              </button>
            </div>
          </div>

          {/* Appearance */}
          <div className="flex items-center justify-between gap-3">
            <span className="text-[13px] text-[#111318] font-semibold">
              {isAr ? 'المظهر' : 'Appearance'}
            </span>
            <div className="flex items-center gap-1 p-1 bg-[#111318]/[0.05] rounded-full">
              {(['system', 'light', 'dark'] as const).map((mode) => (
                <button
                  key={mode}
                  type="button"
                  onClick={() => updateField('appearance', mode)}
                  className={`px-3 py-1 text-[12px] font-semibold rounded-full capitalize transition-all ${
                    settings.appearance === mode
                      ? 'bg-[#111318] text-white shadow-xs'
                      : 'text-[#8A909E]'
                  }`}
                >
                  {mode}
                </button>
              ))}
            </div>
          </div>

          {/* App Accent Color Option Row (opens 10-color popup modal) */}
          <div className="flex items-center justify-between gap-3 py-1">
            <div className="min-w-0">
              <span className="block text-[13px] font-semibold text-[#111318] truncate">
                {isAr ? 'لون التطبيق' : 'App Color'}
              </span>
              <span className="block text-[11.5px] text-[#8A909E] truncate">
                {isAr ? 'اختر من بين ١٠ ألوان مميزة' : 'Choose from 10 accent colors'}
              </span>
            </div>
            <button
              type="button"
              onClick={() => setIsColorModalOpen(true)}
              aria-label={isAr ? 'تغيير لون التطبيق' : 'Change App Color'}
              className="min-h-[40px] pl-2.5 pr-3 py-1.5 rounded-full bg-[#111318]/[0.05] hover:bg-[#111318]/[0.09] text-[#111318] flex items-center gap-2 transition-all active:scale-95 shrink-0"
            >
              <span
                className="w-4 h-4 rounded-full shadow-xs shrink-0"
                style={{ backgroundColor: activeThemeColor.hex }}
              />
              <span className="text-[12px] font-bold">
                {isAr ? activeThemeColor.nameAr : activeThemeColor.nameEn}
              </span>
              <ChevronRight className="w-3.5 h-3.5 text-[#8A909E]" strokeWidth={2} />
            </button>
          </div>

          {/* System Colors (Material You) */}
          <label className="flex items-center justify-between py-1 cursor-pointer">
            <div>
              <span className="block text-[13px] font-semibold text-[#111318]">
                {isAr ? 'ألوان النظام الديناميكية' : 'Material You Dynamic Colors'}
              </span>
              <span className="block text-[11.5px] text-[#8A909E]">
                {isAr ? 'على إصدارات أندرويد المدعومة' : 'Adapt neutral accents on Android 12+'}
              </span>
            </div>
            <input
              type="checkbox"
              checked={settings.dynamicColors}
              onChange={(e) => updateField('dynamicColors', e.target.checked)}
              className="w-4 h-4 accent-[#111318] rounded cursor-pointer"
            />
          </label>

          {/* Prototype Viewport Bezel Toggle */}
          <div className="flex items-center justify-between py-1">
            <div>
              <span className="block text-[13px] font-semibold text-[#111318]">
                {isAr ? 'إطار عرض الهاتف' : 'Android Device Bezel'}
              </span>
              <span className="block text-[11.5px] text-[#8A909E]">
                {isDeviceFrame ? '412 × 844dp standard phone' : 'Expanded tablet/desktop view'}
              </span>
            </div>
            <button
              type="button"
              onClick={onToggleDeviceFrame}
              className="px-3.5 py-1.5 text-[12px] font-bold bg-[#111318] text-white rounded-full hover:bg-neutral-800 transition-colors"
            >
              {isDeviceFrame ? 'Expand' : 'Phone Frame'}
            </button>
          </div>
        </section>

        {/* 11.2 Calling Timing */}
        <section className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] space-y-3.5">
          <h2 className="text-[13.5px] font-bold text-[#111318] tracking-tight">
            {isAr ? '٢. توقيت المكالمات' : 'Calling Timing'}
          </h2>

          {/* Delay Between Calls (0–120s) */}
          <div>
            <div className="flex items-center justify-between text-[12.5px] mb-1">
              <span className="font-semibold text-[#111318]">
                {isAr ? 'الفاصل الزمني بين المكالمات' : 'Delay Between Calls'}
              </span>
              <span className="font-mono font-bold text-[#E2532B] tabular-nums">
                {settings.delayBetweenCalls}s
              </span>
            </div>
            <input
              type="range"
              min={0}
              max={120}
              value={settings.delayBetweenCalls}
              onChange={(e) => updateField('delayBetweenCalls', Number(e.target.value))}
              className="w-full accent-[#E2532B] cursor-pointer"
              aria-label="Delay Between Calls"
            />
            <div className="flex justify-between text-[10.5px] text-[#8A909E] tabular-nums">
              <span>0s</span>
              <span>60s</span>
              <span>120s</span>
            </div>
          </div>

          {/* Random Delay Toggle */}
          <label className="flex items-center justify-between py-1 cursor-pointer">
            <div>
              <span className="block text-[13px] font-semibold text-[#111318]">
                {isAr ? 'تأخير عشوائي إضافي' : 'Random Delay'}
              </span>
              <span className="block text-[11.5px] text-[#8A909E]">
                {isAr ? 'إضافة تفاوت زمني بين المكالمات' : 'Add variable pacing jitter between calls'}
              </span>
            </div>
            <input
              type="checkbox"
              checked={settings.randomDelay}
              onChange={(e) => updateField('randomDelay', e.target.checked)}
              className="w-4 h-4 accent-[#111318] rounded cursor-pointer"
            />
          </label>

          {/* Maximum Random Delay (1–300s when enabled) */}
          {settings.randomDelay && (
            <div className="pl-3 border-l-2 border-[#E2532B]">
              <div className="flex items-center justify-between text-[12px] mb-1">
                <span className="font-semibold text-[#111318]">
                  {isAr ? 'الحد الأقصى للتأخير العشوائي' : 'Maximum Random Delay (1–300s)'}
                </span>
                <span className="font-mono font-bold text-[#111318] tabular-nums">
                  {settings.maxRandomDelay}s
                </span>
              </div>
              <input
                type="range"
                min={1}
                max={300}
                value={settings.maxRandomDelay}
                onChange={(e) => updateField('maxRandomDelay', Number(e.target.value))}
                className="w-full accent-[#E2532B]"
              />
            </div>
          )}

          {/* Countdown Before Start (0–30s) & End Each Call After (0–600s) */}
          <div className="bento-grid-2 pt-1">
            <div>
              <label className="block text-[11.5px] font-medium text-[#8A909E] mb-1 truncate">
                {isAr ? 'العد التنازلي قبل البدء (0-30ث)' : 'Countdown Before Start (0–30s)'}
              </label>
              <input
                type="number"
                min={0}
                max={30}
                value={settings.countdownBeforeStart}
                onChange={(e) =>
                  updateField(
                    'countdownBeforeStart',
                    Math.min(30, Math.max(0, Number(e.target.value)))
                  )
                }
                className="w-full h-10 px-3.5 text-[13px] font-mono font-bold bg-[#111318]/[0.04] rounded-2xl text-[#111318] focus:outline-none"
              />
            </div>

            <div>
              <label className="block text-[11.5px] font-medium text-[#8A909E] mb-1 truncate">
                {isAr ? 'إنهاء المكالمة بعد (0-600ث)' : 'End Each Call After (0–600s)'}
              </label>
              <input
                type="number"
                min={0}
                max={600}
                value={settings.endCallAfterSeconds}
                onChange={(e) =>
                  updateField(
                    'endCallAfterSeconds',
                    Math.min(600, Math.max(0, Number(e.target.value)))
                  )
                }
                className="w-full h-10 px-3.5 text-[13px] font-mono font-bold bg-[#111318]/[0.04] rounded-2xl text-[#111318] focus:outline-none"
              />
            </div>
          </div>

          <p className="text-[11px] text-[#8A909E] leading-relaxed">
            {isAr
              ? 'ملاحظة: القيمة 0 تعطل الحد الزمني. يعتمد الإنهاء التلقائي للمكالمة على قدرات جهاز أندرويد.'
              : 'Setting 0 disables the call time limit. Automatic call termination depends on Android device telephony capabilities.'}
          </p>
        </section>

        {/* 11.3 Campaign Behavior */}
        <section className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] space-y-3">
          <h2 className="text-[13.5px] font-bold text-[#111318] tracking-tight">
            {isAr ? '٣. سلوك الحملة' : 'Campaign Behavior'}
          </h2>

          <div className="bento-grid-2">
            <div>
              <label className="block text-[11.5px] font-medium text-[#8A909E] mb-1 truncate">
                {isAr ? 'إعادة المحاولة لغير المجاب (0-5)' : 'Retry Unanswered (0–5)'}
              </label>
              <input
                type="number"
                min={0}
                max={5}
                value={settings.retryUnanswered}
                onChange={(e) =>
                  updateField(
                    'retryUnanswered',
                    Math.min(5, Math.max(0, Number(e.target.value)))
                  )
                }
                className="w-full h-10 px-3.5 text-[13px] font-mono font-bold bg-[#111318]/[0.04] rounded-2xl text-[#111318] focus:outline-none"
              />
            </div>

            <div>
              <label className="block text-[11.5px] font-medium text-[#8A909E] mb-1 truncate">
                {isAr ? 'جولات الحملة (1-20)' : 'Campaign Rounds (1–20)'}
              </label>
              <input
                type="number"
                min={1}
                max={20}
                value={settings.campaignRounds}
                onChange={(e) =>
                  updateField(
                    'campaignRounds',
                    Math.min(20, Math.max(1, Number(e.target.value)))
                  )
                }
                className="w-full h-10 px-3.5 text-[13px] font-mono font-bold bg-[#111318]/[0.04] rounded-2xl text-[#111318] focus:outline-none"
              />
            </div>
          </div>

          <label className="flex items-center justify-between py-1 cursor-pointer">
            <span className="text-[13px] font-semibold text-[#111318]">
              {isAr ? 'خلط ترتيب جهات الاتصال عشوائياً' : 'Shuffle Contact Order'}
            </span>
            <input
              type="checkbox"
              checked={settings.shuffleContacts}
              onChange={(e) => updateField('shuffleContacts', e.target.checked)}
              className="w-4 h-4 accent-[#111318] rounded cursor-pointer"
            />
          </label>

          <label className="flex items-center justify-between py-1 cursor-pointer">
            <span className="text-[13px] font-semibold text-[#111318]">
              {isAr ? 'تخطي الأرقام التي تم الرد عليها سابقاً' : 'Skip Previously Answered Contacts'}
            </span>
            <input
              type="checkbox"
              checked={settings.skipPreviouslyAnswered}
              onChange={(e) => updateField('skipPreviouslyAnswered', e.target.checked)}
              className="w-4 h-4 accent-[#111318] rounded cursor-pointer"
            />
          </label>

          <label className="flex items-center justify-between py-1 cursor-pointer">
            <div className="pr-3">
              <span className="block text-[13px] font-semibold text-[#111318]">
                {isAr ? 'اكتشاف الرد من سجل المكالمات' : 'Detect Answers from Call Log'}
              </span>
              <span className="block text-[11px] text-[#8A909E] mt-0.5">
                {isAr
                  ? 'يتطلب صلاحية سجل المكالمات؛ بدونها تظل النتيجة "غير مؤكدة".'
                  : 'Requires Call Log permission; otherwise outcomes are recorded as Unverified.'}
              </span>
            </div>
            <input
              type="checkbox"
              checked={settings.detectAnswersFromCallLog}
              onChange={(e) => updateField('detectAnswersFromCallLog', e.target.checked)}
              className="w-4 h-4 accent-[#111318] rounded cursor-pointer shrink-0"
            />
          </label>
        </section>

        {/* 11.4 Audio During Calls */}
        <section className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] space-y-2.5">
          <h2 className="text-[13.5px] font-bold text-[#111318] tracking-tight">
            {isAr ? '٤. الصوت أثناء المكالمات' : 'Audio During Calls'}
          </h2>
          <label className="flex items-center justify-between py-1 cursor-pointer">
            <div className="pr-3">
              <span className="block text-[13px] font-semibold text-[#111318]">
                {isAr
                  ? 'تشغيل مكبر الصوت وكتم الميكروفون تلقائياً'
                  : 'Automatic Speaker and Microphone Mute'}
              </span>
              <span className="block text-[11px] text-[#8A909E] mt-0.5">
                {isAr
                  ? 'قد تقيد بعض الشركات المصنعة للهواتف التحكم المباشر بالصوت.'
                  : 'Some phone manufacturers restrict in-call audio routing control on Android.'}
              </span>
            </div>
            <input
              type="checkbox"
              checked={settings.autoSpeakerAndMute}
              onChange={(e) => updateField('autoSpeakerAndMute', e.target.checked)}
              className="w-4 h-4 accent-[#111318] rounded cursor-pointer shrink-0"
            />
          </label>
        </section>

        {/* 11.5 Call Results and Stages */}
        <section className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] space-y-3">
          <h2 className="text-[13.5px] font-bold text-[#111318] tracking-tight">
            {isAr ? '٥. نتائج ومراحل المكالمات' : 'Call Results & Stages'}
          </h2>

          <label className="flex items-center justify-between py-1 cursor-pointer">
            <div className="pr-3">
              <span className="block text-[13px] font-semibold text-[#111318]">
                {isAr ? 'طلب تصنيف النتيجة بعد المكالمة' : 'Ask for Call Result'}
              </span>
              <span className="block text-[11px] text-[#8A909E] mt-0.5">
                {isAr
                  ? 'يعرض نافذة اختيار المرحلة والملاحظات بعد انتهاء المكالمة قبل الاتصال بالرقم التالي.'
                  : 'Prompts the 5-stage classification sheet after an answered call before dialing the next contact.'}
              </span>
            </div>
            <input
              type="checkbox"
              checked={settings.askForCallResult}
              onChange={(e) => updateField('askForCallResult', e.target.checked)}
              className="w-4 h-4 accent-[#111318] rounded cursor-pointer shrink-0"
            />
          </label>

          <div>
            <div className="flex items-center justify-between text-[12px] mb-1">
              <span className="font-semibold text-[#111318]">
                {isAr ? 'مهلة انتظار اختيار المرحلة (0-60ث)' : 'Wait for Stage Selection (0–60s)'}
              </span>
              <span className="font-mono font-bold text-[#E2532B] tabular-nums">
                {settings.waitForStageSeconds}s
              </span>
            </div>
            <input
              type="range"
              min={0}
              max={60}
              value={settings.waitForStageSeconds}
              onChange={(e) => updateField('waitForStageSeconds', Number(e.target.value))}
              className="w-full accent-[#E2532B]"
            />
          </div>
        </section>

        {/* 11.6 Phone Numbers */}
        <section className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] space-y-2.5">
          <h2 className="text-[13.5px] font-bold text-[#111318] tracking-tight">
            {isAr ? '٦. تنسيق أرقام الهواتف' : 'Phone Numbers'}
          </h2>
          <div className="flex items-center justify-between gap-3">
            <div>
              <span className="block text-[13px] font-semibold text-[#111318]">
                {isAr ? 'مفتاح الدولة الافتراضي' : 'Default Country Code'}
              </span>
              <span className="block text-[11px] text-[#8A909E]">
                {isAr
                  ? 'يصحح الأرقام المحلية تلقائياً (مثال: 20 لمصر)'
                  : 'Normalizes local number formats automatically (e.g. 20 for Egypt)'}
              </span>
            </div>
            <input
              type="text"
              value={settings.defaultCountryCode}
              onChange={(e) =>
                updateField('defaultCountryCode', e.target.value.replace(/[^\d]/g, ''))
              }
              placeholder="20"
              className="w-20 h-10 px-2.5 text-center font-mono font-bold text-[13px] bg-[#111318]/[0.04] rounded-2xl text-[#111318] focus:outline-none"
            />
          </div>
        </section>

        {/* 11.7 Allowed Calling Hours */}
        <section className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] space-y-3">
          <h2 className="text-[13.5px] font-bold text-[#111318] tracking-tight">
            {isAr ? '٧. ساعات الاتصال المسموحة' : 'Allowed Calling Hours'}
          </h2>

          <label className="flex items-center justify-between py-1 cursor-pointer">
            <span className="text-[13px] font-semibold text-[#111318]">
              {isAr ? 'تقييد المكالمات بساعات محددة' : 'Restrict Calls to Specific Hours'}
            </span>
            <input
              type="checkbox"
              checked={settings.restrictCallingHours}
              onChange={(e) => updateField('restrictCallingHours', e.target.checked)}
              className="w-4 h-4 accent-[#111318] rounded cursor-pointer"
            />
          </label>

          {settings.restrictCallingHours && (
            <>
              <div className="bento-grid-2">
                <div>
                  <label className="block text-[11.5px] font-medium text-[#8A909E] mb-1">
                    {isAr ? 'من الساعة (0-23)' : 'From Hour (0–23)'}
                  </label>
                  <input
                    type="number"
                    min={0}
                    max={23}
                    value={settings.allowedHourFrom}
                    onChange={(e) =>
                      updateField(
                        'allowedHourFrom',
                        Math.min(23, Math.max(0, Number(e.target.value)))
                      )
                    }
                    className="w-full h-10 px-3.5 font-mono font-bold text-[13px] bg-[#111318]/[0.04] rounded-2xl text-[#111318] focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-[11.5px] font-medium text-[#8A909E] mb-1">
                    {isAr ? 'إلى الساعة (0-23)' : 'To Hour (0–23)'}
                  </label>
                  <input
                    type="number"
                    min={0}
                    max={23}
                    value={settings.allowedHourTo}
                    onChange={(e) =>
                      updateField(
                        'allowedHourTo',
                        Math.min(23, Math.max(0, Number(e.target.value)))
                      )
                    }
                    className="w-full h-10 px-3.5 font-mono font-bold text-[13px] bg-[#111318]/[0.04] rounded-2xl text-[#111318] focus:outline-none"
                  />
                </div>
              </div>

              {isSameCallingHour && (
                <div className="p-3 bg-amber-50 rounded-2xl flex items-start gap-2 text-[11.5px] text-[#B45309]">
                  <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5" />
                  <span>
                    {isAr
                      ? 'تحذير: ساعة البداية والنهاية متطابقتان. لن يتم إجراء أي مكالمات.'
                      : 'Warning: Start and end hours are identical. Calling window is effectively zero hours.'}
                  </span>
                </div>
              )}
            </>
          )}
        </section>

        {/* 11.8 Screen and Notifications */}
        <section className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] space-y-2.5">
          <h2 className="text-[13.5px] font-bold text-[#111318] tracking-tight">
            {isAr ? '٨. الشاشة والتنبيهات' : 'Screen & Notifications'}
          </h2>

          <label className="flex items-center justify-between py-1 cursor-pointer">
            <span className="text-[13px] font-semibold text-[#111318]">
              {isAr ? 'إبقاء الشاشة قيد التشغيل أثناء الاتصال' : 'Keep Screen On During Calling'}
            </span>
            <input
              type="checkbox"
              checked={settings.keepScreenOn}
              onChange={(e) => updateField('keepScreenOn', e.target.checked)}
              className="w-4 h-4 accent-[#111318] rounded cursor-pointer"
            />
          </label>

          <label className="flex items-center justify-between py-1 cursor-pointer">
            <span className="text-[13px] font-semibold text-[#111318]">
              {isAr ? 'اهتزاز عند انتهاء الحملة' : 'Vibrate When Campaign Ends'}
            </span>
            <input
              type="checkbox"
              checked={settings.vibrateOnCampaignEnd}
              onChange={(e) => updateField('vibrateOnCampaignEnd', e.target.checked)}
              className="w-4 h-4 accent-[#111318] rounded cursor-pointer"
            />
          </label>
        </section>

        {/* 11.9 Android Permissions */}
        <section className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="text-[13.5px] font-bold text-[#111318] tracking-tight">
              {isAr ? '٩. صلاحيات نظام أندرويد' : 'Android Permissions'}
            </h2>
            <span className="text-[11px] text-[#8A909E]">
              {isAr ? 'اضغط لمحاكاة الحالة' : 'Tap status to inspect state'}
            </span>
          </div>

          <div className="divide-y divide-black/[0.04] bg-[#111318]/[0.02] rounded-2xl overflow-hidden">
            {(
              [
                { key: 'calling' as const, label: 'Calling (CALL_PHONE)' },
                { key: 'phoneState' as const, label: 'Phone State (READ_PHONE_STATE)' },
                { key: 'callLog' as const, label: 'Call Log (READ_CALL_LOG)' },
                { key: 'endingCalls' as const, label: 'Ending Calls (ANSWER_PHONE_CALLS)' },
                { key: 'phoneContacts' as const, label: 'Phone Contacts (READ_CONTACTS)' },
              ]
            ).map((perm) => {
              const granted = settings.permissions[perm.key];
              return (
                <div
                  key={perm.key}
                  className="px-3.5 py-2.5 flex items-center justify-between gap-2"
                >
                  <div className="flex items-center gap-2 min-w-0">
                    {granted ? (
                      <ShieldCheck className="w-4 h-4 text-[#047857] shrink-0" />
                    ) : (
                      <ShieldAlert className="w-4 h-4 text-[#B45309] shrink-0" />
                    )}
                    <span className="text-[12.5px] font-semibold text-[#111318] truncate">
                      {perm.label}
                    </span>
                  </div>
                  <button
                    type="button"
                    onClick={() => togglePermission(perm.key)}
                    className={`text-[11px] font-bold px-3 py-1 rounded-full transition-colors ${
                      granted
                        ? 'bg-emerald-500/12 text-[#047857]'
                        : 'bg-amber-500/15 text-[#B45309]'
                    }`}
                  >
                    {granted ? 'Granted' : 'Missing'}
                  </button>
                </div>
              );
            })}
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() =>
                onTriggerToast(
                  'info',
                  'Opens native Android App Info → Permissions screen on device.'
                )
              }
              className="flex-1 min-h-[40px] py-2 px-3 rounded-full bg-[#111318] text-white text-[12px] font-bold hover:bg-neutral-800 transition-colors"
            >
              Open Android App Settings
            </button>
            <button
              type="button"
              onClick={() => setShowBatteryGuidance(!showBatteryGuidance)}
              className="min-h-[40px] py-2 px-3.5 rounded-full bg-[#111318]/[0.05] text-[12px] font-semibold text-[#111318] hover:bg-[#111318]/[0.08]"
            >
              Battery Guidance
            </button>
          </div>

          {showBatteryGuidance && (
            <div className="p-3 bg-[#111318]/[0.04] rounded-2xl text-[11.5px] text-[#4B5563] leading-relaxed">
              <strong className="text-[#111318] block mb-0.5">
                Unrestricted Background Operation
              </strong>
              To prevent Android OEM battery savers from pausing multi-call queues when the screen dims, set O2Dialer battery usage to <strong>Unrestricted</strong> in Android App Info.
            </div>
          )}
        </section>

        {/* 11.10 Device Activation (Optional Licensing System) */}
        <section className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="text-[13.5px] font-bold text-[#111318] tracking-tight">
              {isAr ? '١٠. تفعيل الجهاز (اختياري)' : 'Device Activation'}
            </h2>
            <label className="flex items-center gap-1.5 text-[11.5px] font-medium text-[#8A909E] cursor-pointer">
              <span>Enable licensing</span>
              <input
                type="checkbox"
                checked={settings.licensingEnabled}
                onChange={(e) => updateField('licensingEnabled', e.target.checked)}
                className="w-3.5 h-3.5 accent-[#111318] rounded"
              />
            </label>
          </div>

          {!settings.licensingEnabled ? (
            <p className="text-[12px] text-[#8A909E]">
              Device licensing is currently disabled for this build. Toggle above to preview activation states.
            </p>
          ) : (
            <div className="p-3.5 bg-[#111318]/[0.03] rounded-2xl space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <KeyRound className="w-4 h-4 text-[#111318]" />
                  <span className="text-[12.5px] font-bold text-[#111318]">
                    License Status
                  </span>
                </div>
                <span
                  className={`text-[11.5px] font-bold capitalize ${
                    settings.licenseStatus === 'activated'
                      ? 'text-[#047857]'
                      : settings.licenseStatus === 'suspended' ||
                        settings.licenseStatus === 'error'
                      ? 'text-[#B42318]'
                      : 'text-[#B45309]'
                  }`}
                >
                  {settings.licenseStatus.replace('_', ' ')}
                </span>
              </div>

              <div className="flex items-center gap-2">
                <input
                  type="text"
                  value={settings.activationCode}
                  onChange={(e) => updateField('activationCode', e.target.value)}
                  placeholder="Enter Activation Code"
                  className="flex-1 h-10 px-3.5 text-[12.5px] font-mono bg-white rounded-full text-[#111318] focus:outline-none"
                />
                <button
                  type="button"
                  onClick={handleVerifyLicense}
                  className="h-10 px-4 rounded-full bg-[#111318] text-white text-[12px] font-bold whitespace-nowrap"
                >
                  {settings.licenseStatus === 'checking' ? 'Checking…' : 'Verify'}
                </button>
              </div>

              <div className="flex items-center gap-1.5 flex-wrap pt-1">
                {(['not_activated', 'activated', 'suspended', 'error'] as const).map((st) => (
                  <button
                    key={st}
                    type="button"
                    onClick={() => updateField('licenseStatus', st)}
                    className={`px-2.5 py-1 text-[10.5px] rounded-full capitalize ${
                      settings.licenseStatus === st
                        ? 'bg-[#111318] font-bold text-white'
                        : 'text-[#8A909E] hover:text-[#111318]'
                    }`}
                  >
                    {st.replace('_', ' ')}
                  </button>
                ))}
              </div>
            </div>
          )}
        </section>

        {/* 11.11 Application Updates */}
        <section className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] space-y-3">
          <div className="flex items-center justify-between gap-2">
            <div>
              <h2 className="text-[13.5px] font-bold text-[#111318] tracking-tight">
                {isAr ? '١١. تحديثات التطبيق' : 'Application Updates'}
              </h2>
              <p className="text-[11.5px] text-[#8A909E] font-mono">
                Installed Version: 7.0.0 (Signed Release)
              </p>
            </div>

            {updateState === 'idle' || updateState === 'up_to_date' ? (
              <button
                type="button"
                onClick={handleCheckForUpdates}
                className="min-h-[38px] px-3.5 rounded-full bg-[#111318] text-white text-[11.5px] font-bold hover:bg-neutral-800 flex items-center gap-1.5 shrink-0"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span>Check for Updates</span>
              </button>
            ) : null}
          </div>

          {updateState !== 'idle' && (
            <div className="p-3.5 bg-[#111318]/[0.03] rounded-2xl space-y-2.5">
              {updateState === 'checking' && (
                <div className="text-[12.5px] text-[#8A909E] flex items-center gap-2">
                  <RefreshCw className="w-4 h-4 animate-spin text-[#111318]" />
                  <span>Checking verified GitHub release feed…</span>
                </div>
              )}

              {updateState === 'available' && (
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-[13px] font-bold text-[#111318]">
                      Update Available · O2Dialer 7.1.0
                    </span>
                    <span className="text-[11px] font-mono text-[#047857]">SHA-256 Verified</span>
                  </div>
                  <p className="text-[11.5px] text-[#8A909E]">
                    Includes improved dual-SIM call log matching and faster CSV parsing. Local campaigns and settings are preserved.
                  </p>
                  <div className="flex items-center gap-2 pt-1">
                    <button
                      type="button"
                      onClick={handleDownloadUpdate}
                      className="h-9 px-3.5 rounded-full bg-[#111318] text-white text-[12px] font-bold flex items-center gap-1.5"
                    >
                      <Download className="w-3.5 h-3.5" />
                      <span>Download Update (14.2 MB)</span>
                    </button>
                    <button
                      type="button"
                      onClick={() => setUpdateState('error')}
                      className="h-9 px-3 rounded-full bg-[#111318]/[0.05] text-[11.5px] font-semibold text-[#8A909E]"
                    >
                      Simulate Failure
                    </button>
                  </div>
                </div>
              )}

              {(updateState === 'downloading' || updateState === 'verifying') && (
                <div className="space-y-2">
                  <div className="flex items-center justify-between text-[12px]">
                    <span className="font-semibold text-[#111318]">
                      {updateState === 'verifying'
                        ? 'Verifying APK signature…'
                        : 'Downloading O2Dialer-7.1.0.apk…'}
                    </span>
                    <span className="font-mono tabular-nums font-bold">
                      {downloadProgress}%
                    </span>
                  </div>
                  <div className="w-full h-1.5 bg-[#111318]/[0.08] rounded-full overflow-hidden">
                    <div
                      className="h-full bg-[#E2532B] transition-all duration-300"
                      style={{ width: `${downloadProgress}%` }}
                    />
                  </div>
                </div>
              )}

              {updateState === 'ready_to_install' && (
                <div className="space-y-2">
                  <div className="flex items-center gap-1.5 text-[12.5px] font-bold text-[#047857]">
                    <CheckCircle2 className="w-4 h-4" />
                    <span>Ready to Install · Signature Verified</span>
                  </div>
                  <p className="text-[11.5px] text-[#8A909E]">
                    Android requires explicit confirmation through the system package installer. Your existing campaigns and call history will be preserved.
                  </p>
                  <div className="flex items-center gap-2 pt-1">
                    <button
                      type="button"
                      onClick={() => setShowInstallGuidance(true)}
                      className="h-9 px-3.5 rounded-full bg-[#111318] text-white text-[12px] font-bold"
                    >
                      Launch Android Installer
                    </button>
                    <button
                      type="button"
                      onClick={() => setUpdateState('up_to_date')}
                      className="h-9 px-3 rounded-full bg-[#111318]/[0.05] text-[11.5px] font-semibold text-[#8A909E]"
                    >
                      Mark Up-to-Date
                    </button>
                  </div>
                  {showInstallGuidance && (
                    <div className="p-3 bg-white rounded-2xl text-[11.5px] text-[#4B5563] mt-2">
                      <strong className="text-[#111318] block">
                        Android Installation Permission Guidance
                      </strong>
                      If prompted by Android, enable <em>Allow from this source (REQUEST_INSTALL_PACKAGES)</em> and tap <strong>Update</strong> in the native system dialog.
                    </div>
                  )}
                </div>
              )}

              {updateState === 'error' && (
                <div className="space-y-2">
                  <div className="text-[12.5px] font-bold text-[#B42318]">
                    Update Download Failed
                  </div>
                  <p className="text-[11.5px] text-[#8A909E]">
                    Could not verify release package or network connection was interrupted.
                  </p>
                  <button
                    type="button"
                    onClick={handleCheckForUpdates}
                    className="h-9 px-3.5 rounded-full bg-[#111318] text-white text-[12px] font-bold"
                  >
                    Retry Update Check
                  </button>
                </div>
              )}
            </div>
          )}
        </section>

        {/* 11.12 Application Information & Developer Handoff */}
        <footer className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] text-center space-y-2">
          <div className="text-[13px] font-extrabold text-[#111318]">
            O2Dialer · Version 7.0
          </div>
          <p className="text-[11px] text-[#8A909E]">
            Organize. Call. Track. Follow up. — Native Android Telephony Productivity
          </p>
          <button
            type="button"
            onClick={onOpenHandoffSpec}
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-full bg-[#111318] text-white text-[11.5px] font-bold hover:bg-neutral-800 transition-colors mt-1"
          >
            <FileCode2 className="w-3.5 h-3.5" />
            <span>Kotlin / Jetpack Compose Handoff Spec</span>
          </button>
        </footer>
      </div>

      {/* 10-Color Picker Popup Window */}
      {isColorModalOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-5 bg-[#111318]/40 backdrop-blur-sm animate-in fade-in duration-150"
          onClick={() => setIsColorModalOpen(false)}
        >
          <div
            role="dialog"
            aria-modal="true"
            aria-label={isAr ? 'اختر لون التطبيق' : 'Choose App Color'}
            onClick={(e) => e.stopPropagation()}
            className="w-full max-w-[360px] rounded-[28px] bg-white/95 backdrop-blur-2xl p-5 shadow-[0_24px_60px_-12px_rgba(17,19,24,0.28)] space-y-4"
          >
            <div className="flex items-center justify-between gap-2">
              <div className="flex items-center gap-2 min-w-0">
                <div
                  className="w-8 h-8 rounded-full flex items-center justify-center text-white shrink-0 shadow-xs"
                  style={{ backgroundColor: activeThemeColor.hex }}
                >
                  <Palette className="w-4 h-4" strokeWidth={2} />
                </div>
                <div className="min-w-0">
                  <h3 className="text-[15px] font-extrabold text-[#111318] leading-tight truncate">
                    {isAr ? 'اختر لون التطبيق' : 'Choose App Color'}
                  </h3>
                  <p className="text-[11px] text-[#8A909E] truncate">
                    {isAr
                      ? 'اضغط على أي لون لتطبيقه وإغلاق النافذة'
                      : 'Tap a color to apply and close'}
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setIsColorModalOpen(false)}
                aria-label="Close color picker"
                className="w-9 h-9 rounded-full bg-[#111318]/[0.05] hover:bg-[#111318]/[0.1] flex items-center justify-center text-[#111318] transition-colors shrink-0"
              >
                <X className="w-4 h-4" strokeWidth={2} />
              </button>
            </div>

            <div
              className="grid grid-cols-2 gap-2.5"
              role="radiogroup"
              aria-label={isAr ? 'ألوان التطبيق' : 'App Colors'}
            >
              {APP_THEME_COLORS.map((colorItem) => {
                const isSelected = activeThemeColor.id === colorItem.id;
                const label = isAr ? colorItem.nameAr : colorItem.nameEn;
                return (
                  <button
                    key={colorItem.id}
                    type="button"
                    role="radio"
                    aria-checked={isSelected}
                    aria-label={label}
                    onClick={() => {
                      updateField('appColor', colorItem.id);
                      setIsColorModalOpen(false);
                      onTriggerToast(
                        'info',
                        isAr
                          ? `تم تغيير لون التطبيق إلى ${colorItem.nameAr}`
                          : `App accent color set to ${colorItem.nameEn}`
                      );
                    }}
                    className={`min-h-[48px] px-3 py-2 rounded-[18px] flex items-center justify-between gap-2.5 text-left transition-all active:scale-95 ${
                      isSelected
                        ? 'bg-[#111318] text-white shadow-[0_10px_24px_-6px_rgba(17,19,24,0.28)]'
                        : 'bg-[#111318]/[0.04] hover:bg-[#111318]/[0.08] text-[#111318]'
                    }`}
                  >
                    <div className="flex items-center gap-2.5 min-w-0">
                      <span
                        className="w-6 h-6 rounded-full flex items-center justify-center shrink-0 shadow-xs"
                        style={{ backgroundColor: colorItem.hex }}
                      >
                        {isSelected && <Check className="w-3.5 h-3.5 text-white stroke-[2.75]" />}
                      </span>
                      <span className="text-[12px] font-bold truncate">{label}</span>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
