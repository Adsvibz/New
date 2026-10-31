import React, { useState, useEffect } from 'react';
import {
  Megaphone,
  PhoneCall,
  BarChart3,
  Globe,
  ArrowRight,
  ArrowLeft,
  CheckCircle2,
  Sparkles,
  FileSpreadsheet,
  Users,
  Pause,
  SkipForward,
  Square,
  MessageCircle,
  Share2,
  ShieldCheck,
  Zap,
} from 'lucide-react';
import { Language } from '../i18n/translations';

interface OnboardingModalProps {
  isOpen: boolean;
  currentLanguage: Language;
  onToggleLanguage: () => void;
  onComplete: () => void;
}

export const OnboardingModal: React.FC<OnboardingModalProps> = ({
  isOpen,
  currentLanguage,
  onToggleLanguage,
  onComplete,
}) => {
  const [step, setStep] = useState<number>(0);
  const [liveTimer, setLiveTimer] = useState<number>(14);

  // Reset to Screen 1 whenever opened
  useEffect(() => {
    if (isOpen) {
      setStep(0);
    }
  }, [isOpen]);

  // Subtle live timer animation for Screen 2's Live Dialer preview
  useEffect(() => {
    if (!isOpen || step !== 1) return;
    const interval = setInterval(() => {
      setLiveTimer((prev) => (prev >= 29 ? 12 : prev + 1));
    }, 1000);
    return () => clearInterval(interval);
  }, [isOpen, step]);

  if (!isOpen) return null;

  const isAr = currentLanguage === 'ar';

  const screens = [
    {
      id: 'campaigns',
      badge: isAr ? '٠١ / ٠٣ · إدارة ذكية' : '01 / 03 · SMART CAMPAIGNS',
      title: isAr
        ? 'نظّم حملاتك وجهات اتصالك في ثوانٍ'
        : 'Build & Launch Calling Campaigns in Seconds',
      subtitle: isAr
        ? 'صمّم قوائم اتصال احترافية، استورد آلاف الأرقام من ملفات CSV أو الصقها مباشرة مع تنظيف تلقائي للأرقام المكررة.'
        : 'Import contacts via CSV/TXT, paste multiline phone lists, assign dual-SIM slots, and automatically clean duplicates.',
      features: isAr
        ? [
            {
              icon: FileSpreadsheet,
              title: 'استيراد فوري ولصق ذكي',
              desc: 'دعم ملفات CSV و TXT أو لصق القوائم النصية مباشرة',
            },
            {
              icon: Users,
              title: 'كشف التكرار تلقائياً',
              desc: 'تنظيف الأرقام المكررة بضغطة واحدة قبل بدء الاتصال',
            },
            {
              icon: ShieldCheck,
              title: 'تخصيص شريحة الاتصال (SIM 1 / SIM 2)',
              desc: 'اختر الشريحة المناسبة لكل حملة أو اترك الاختيار للنظام',
            },
          ]
        : [
            {
              icon: FileSpreadsheet,
              title: 'Instant CSV Import & Smart Paste',
              desc: 'Drop CSV/TXT lists or paste raw numbers with auto-formatting',
            },
            {
              icon: Users,
              title: 'One-Tap Duplicate Cleaner',
              desc: 'Automatically detect and merge duplicate phone entries',
            },
            {
              icon: ShieldCheck,
              title: 'Dedicated Dual-SIM Routing',
              desc: 'Bind each campaign to SIM 1, SIM 2, or System Default',
            },
          ],
    },
    {
      id: 'dialer',
      badge: isAr ? '٠٢ / ٠٣ · المتصل الآلي الحي' : '02 / 03 · LIVE AUTO-DIALER',
      title: isAr
        ? 'اتصال آلي سلس بتحكم كامل داخل المكالمة'
        : 'Effortless Hands-Free Calling Workflow',
      subtitle: isAr
        ? 'يتولى O2Dialer طلب الأرقام بالتتابع مع فاصل زمني ذكي، وأزرار تحكم فورية مدمجة داخل بطاقة الاتصال الحية.'
        : 'Run automated calling queues with smart pacing intervals and integrated in-call controls right inside the live box.',
      features: isAr
        ? [
            {
              icon: PhoneCall,
              title: 'صندوق اتصال حي متكامل',
              desc: 'تخطي الرقم، إيقاف مؤقت، أو إنهاء الجلسة مباشرة أثناء الاتصال',
            },
            {
              icon: Zap,
              title: 'فاصل زمني ذكي وإعادة المحاولة',
              desc: 'تحكم بالثواني بين كل مكالمة مع جدولة الأرقام التي لم ترد',
            },
            {
              icon: Sparkles,
              title: 'حفظ فوري للتقدم',
              desc: 'توقف واستأنف حملتك في أي وقت دون فقدان أي سجل',
            },
          ]
        : [
            {
              icon: PhoneCall,
              title: 'Unified Live Call Box & Controls',
              desc: 'Skip, Pause, or Stop right inside the active call card',
            },
            {
              icon: Zap,
              title: 'Smart Pacing & Auto-Retry',
              desc: 'Custom delay timers between calls with randomized intervals',
            },
            {
              icon: Sparkles,
              title: 'Zero-Loss Session Persistence',
              desc: 'Pause and resume any campaign right where you left off',
            },
          ],
    },
    {
      id: 'results',
      badge: isAr ? '٠٣ / ٠٣ · النتائج والمتابعة' : '03 / 03 · RESULTS & FOLLOW-UP',
      title: isAr
        ? 'حوّل كل مكالمة إلى فرصة حقيقية'
        : 'Turn Every Conversation Into Action',
      subtitle: isAr
        ? 'صنّف العملاء بعد كل مكالمة، دوّن ملاحظاتك، وأرسل رسائل متابعة عبر واتساب أو صدّر تقارير CSV بضغطة واحدة.'
        : 'Classify call outcomes into 5 clear stages, capture notes, trigger instant WhatsApp follow-ups, and export CSV reports.',
      features: isAr
        ? [
            {
              icon: BarChart3,
              title: '٥ مراحل لتصنيف العملاء',
              desc: 'عميل محتمل، معاودة اتصال، واتساب، بريد صوتي، أو غير مهتم',
            },
            {
              icon: MessageCircle,
              title: 'متابعة فورية عبر واتساب',
              desc: 'افتح محادثة واتساب مباشرة مع الرقم دون حفظه في هاتفك',
            },
            {
              icon: Share2,
              title: 'تصدير ومشاركة التقارير',
              desc: 'احصل على ملخص شامل للحملة أو صدّر ملف CSV كامل',
            },
          ]
        : [
            {
              icon: BarChart3,
              title: '5-Stage Lead Classification',
              desc: 'Tag Potential, Call Back, WhatsApp, Voicemail, or Not Interested',
            },
            {
              icon: MessageCircle,
              title: 'One-Tap WhatsApp Follow-Up',
              desc: 'Jump straight into a WhatsApp chat after any call',
            },
            {
              icon: Share2,
              title: 'Elegant Analytics & CSV Export',
              desc: 'Review answer rates and share full campaign summaries',
            },
          ],
    },
  ];

  const current = screens[step];

  return (
    <div
      dir={isAr ? 'rtl' : 'ltr'}
      className="absolute inset-0 z-20 app-device-canvas flex flex-col justify-between overflow-hidden select-none"
      role="dialog"
      aria-modal="true"
      aria-labelledby="welcome-screen-title"
    >
      {/* Top Bar (with clearance for AndroidStatusBar) */}
      <div className="pt-9 px-[20px] pb-2 flex items-center justify-between shrink-0 z-10">
        <div className="flex items-center gap-2">
          <span className="w-2.5 h-2.5 rounded-full bg-[#E2532B] shadow-[0_0_10px_var(--app-accent)]" />
          <span className="text-[13px] font-extrabold text-[#111318] tracking-tight">
            O2Dialer 7.0
          </span>
        </div>

        <div className="flex items-center gap-1.5">
          <button
            type="button"
            onClick={onToggleLanguage}
            className="min-h-[34px] px-3 rounded-full bg-white/75 border border-white/90 text-[11.5px] font-semibold text-[#111318] hover:bg-white transition-all flex items-center gap-1.5 shadow-xs"
          >
            <Globe className="w-3.5 h-3.5 text-[#6E7582]" strokeWidth={1.75} />
            <span>{isAr ? 'English' : 'العربية'}</span>
          </button>

          <button
            type="button"
            onClick={onComplete}
            className="min-h-[34px] px-3 rounded-full text-[11.5px] font-semibold text-[#6E7582] hover:text-[#111318] hover:bg-black/[0.04] transition-colors"
          >
            {isAr ? 'تخطي' : 'Skip'}
          </button>
        </div>
      </div>

      {/* Main Animated Stage */}
      <div
        key={step}
        className="flex-1 px-[20px] py-1 flex flex-col justify-between overflow-y-auto no-scrollbar animate-welcome-slide"
        style={{ paddingTop: '4px' }}
      >
        {/* Interactive Visual Showcase Box for Each Step */}
        <div className="relative w-full rounded-[28px] bg-white/60 border border-white/90 p-4 shadow-[0_18px_44px_-12px_rgba(17,19,24,0.08)] overflow-hidden shrink-0">
          {/* Soft decorative accent aura inside showcase */}
          <div
            aria-hidden="true"
            className="pointer-events-none absolute -top-10 -right-10 w-36 h-36 rounded-full bg-[#E2532B]/12 blur-2xl"
          />

          {step === 0 && (
            <div className="relative space-y-2.5 py-1">
              {/* Animated Campaign Preview Card */}
              <div className="rounded-[20px] bg-white/95 p-3.5 shadow-[0_10px_28px_-8px_rgba(17,19,24,0.08)] border border-black/[0.03] animate-welcome-float">
                <div className="flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2 min-w-0">
                    <div className="w-8 h-8 rounded-xl bg-[#E2532B] text-white flex items-center justify-center shrink-0 shadow-xs">
                      <Megaphone className="w-4 h-4" strokeWidth={2} />
                    </div>
                    <div className="min-w-0">
                      <div className="text-[13px] font-extrabold text-[#111318] truncate">
                        {isAr ? 'حملة كبار العملاء Q4' : 'Enterprise VIP Outbound Q4'}
                      </div>
                      <div className="text-[10.5px] text-[#6E7582] font-medium">
                        {isAr ? '١٢٠ جهة اتصال · شريحة 1' : '120 contacts · SIM 1'}
                      </div>
                    </div>
                  </div>
                  <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/12 text-emerald-700 shrink-0">
                    {isAr ? 'نشطة' : 'Active'}
                  </span>
                </div>

                {/* Animated Progress Bar */}
                <div className="mt-3">
                  <div className="flex items-center justify-between text-[10.5px] font-bold mb-1">
                    <span className="text-[#6E7582]">
                      {isAr ? 'تم الاتصال بـ ٨٢ من ١٢٠' : '82 / 120 called'}
                    </span>
                    <span className="text-[#111318] tabular-nums">68%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-[#111318]/[0.06] overflow-hidden">
                    <div className="h-full rounded-full bg-[#E2532B] animate-welcome-progress" />
                  </div>
                </div>
              </div>

              {/* Floating Smart Badges Row */}
              <div className="grid grid-cols-3 gap-2 animate-welcome-float-delayed">
                <div className="rounded-2xl bg-white/90 px-2.5 py-2 border border-black/[0.03] text-center shadow-2xs">
                  <div className="text-[11px] font-extrabold text-[#111318]">CSV / TXT</div>
                  <div className="text-[9.5px] text-[#6E7582] mt-0.5">
                    {isAr ? 'استيراد سريع' : 'Fast Import'}
                  </div>
                </div>
                <div className="rounded-2xl bg-[#111318] text-white px-2.5 py-2 text-center shadow-xs">
                  <div className="text-[11px] font-extrabold">SIM 1 & 2</div>
                  <div className="text-[9.5px] text-white/75 mt-0.5">
                    {isAr ? 'توجيه ذكي' : 'Dual Routing'}
                  </div>
                </div>
                <div className="rounded-2xl bg-white/90 px-2.5 py-2 border border-black/[0.03] text-center shadow-2xs">
                  <div className="text-[11px] font-extrabold text-emerald-700">0 Dupes</div>
                  <div className="text-[9.5px] text-[#6E7582] mt-0.5">
                    {isAr ? 'تنظيف تلقائي' : 'Auto-Clean'}
                  </div>
                </div>
              </div>
            </div>
          )}

          {step === 1 && (
            <div className="relative py-1">
              {/* Simulated Live Call Box with Integrated Actions */}
              <div className="rounded-[22px] bg-white/95 p-3.5 text-center shadow-[0_12px_30px_-8px_rgba(17,19,24,0.08)] border border-black/[0.03] animate-welcome-float">
                <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-700 text-[10px] font-bold mb-1.5">
                  <span className="relative flex h-2 w-2">
                    <span className="animate-welcome-ring absolute inline-flex h-full w-full rounded-full bg-emerald-500 opacity-75" />
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-600" />
                  </span>
                  <span>
                    {isAr ? 'متصل الآن · جاري التحدث' : 'Current Contact · Connected'}
                  </span>
                </div>

                <div className="text-[17px] font-extrabold text-[#111318] tracking-tight">
                  {isAr ? 'جيمس ويلسون' : 'James Wilson'}
                </div>
                <div className="text-[11px] font-mono text-[#6E7582]" dir="ltr">
                  +1 (202) 555-0168 · SIM 1
                </div>

                <div
                  className="mt-1.5 text-[22px] font-extrabold font-mono text-[#111318] tabular-nums tracking-tight"
                  dir="ltr"
                >
                  00:{liveTimer < 10 ? `0${liveTimer}` : liveTimer}
                </div>

                {/* Integrated Call Actions inside Live Box */}
                <div className="mt-2.5 pt-2.5 border-t border-[#111318]/[0.06] grid grid-cols-3 gap-1.5">
                  <div className="h-8 rounded-full bg-[#111318]/[0.05] text-[#111318] flex items-center justify-center gap-1 text-[10px] font-bold">
                    <SkipForward className="w-3 h-3" />
                    <span>{isAr ? 'تخطي' : 'Skip'}</span>
                  </div>
                  <div className="h-8 rounded-full bg-[#111318] text-white flex items-center justify-center gap-1 text-[10px] font-bold shadow-xs">
                    <Pause className="w-3 h-3 fill-current" />
                    <span>{isAr ? 'إيقاف مؤقت' : 'Pause'}</span>
                  </div>
                  <div className="h-8 rounded-full bg-[#E2532B]/12 text-[#E2532B] flex items-center justify-center gap-1 text-[10px] font-bold">
                    <Square className="w-2.5 h-2.5 fill-current" />
                    <span>{isAr ? 'إيقاف' : 'Stop'}</span>
                  </div>
                </div>
              </div>
            </div>
          )}

          {step === 2 && (
            <div className="relative space-y-2.5 py-1">
              {/* Conversion Rate + Stage Classification Preview */}
              <div className="rounded-[20px] bg-white/95 p-3.5 shadow-[0_10px_28px_-8px_rgba(17,19,24,0.08)] border border-black/[0.03] flex items-center justify-between gap-3 animate-welcome-float">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-2xl bg-[#E2532B] text-white flex flex-col items-center justify-center shrink-0 shadow-xs">
                    <span className="text-[14px] font-extrabold leading-none">78%</span>
                    <span className="text-[8.5px] font-semibold uppercase opacity-85 mt-0.5">
                      {isAr ? 'ردود' : 'Reach'}
                    </span>
                  </div>
                  <div>
                    <div className="text-[13px] font-extrabold text-[#111318]">
                      {isAr ? 'تحليل فوري للنتائج' : 'Instant Outcome Analytics'}
                    </div>
                    <div className="text-[10.5px] text-[#6E7582] mt-0.5">
                      {isAr
                        ? 'تصنيف المراحل + تصدير تقارير CSV'
                        : '5 Lead Stages · 1-Tap CSV Export'}
                    </div>
                  </div>
                </div>
                <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" />
              </div>

              {/* Stage Pills + WhatsApp Action Preview */}
              <div className="flex items-center justify-between gap-2 animate-welcome-float-delayed">
                <div className="flex items-center gap-1.5 flex-wrap">
                  <span className="px-2.5 py-1 rounded-full bg-emerald-500/12 text-emerald-700 text-[10px] font-bold">
                    {isAr ? 'عميل محتمل' : 'Potential'}
                  </span>
                  <span className="px-2.5 py-1 rounded-full bg-amber-500/12 text-amber-700 text-[10px] font-bold">
                    {isAr ? 'معاودة اتصال' : 'Call Back'}
                  </span>
                </div>
                <div className="px-3 py-1 rounded-full bg-[#111318] text-white text-[10px] font-bold flex items-center gap-1 shadow-xs shrink-0">
                  <MessageCircle className="w-3 h-3 text-emerald-400" />
                  <span>WhatsApp</span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Editorial Copy & 3 Feature Cards */}
        <div className="my-auto py-2">
          <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#E2532B]/10 text-[#E2532B] text-[10px] font-extrabold tracking-wider uppercase mb-2">
            <Sparkles className="w-3 h-3" strokeWidth={2} />
            <span>{current.badge}</span>
          </div>

          <h1
            id="welcome-screen-title"
            className="text-[22px] font-extrabold text-[#111318] tracking-tight leading-[1.2]"
          >
            {current.title}
          </h1>

          <p className="text-[12.5px] text-[#6E7582] mt-1.5 leading-relaxed">
            {current.subtitle}
          </p>

          {/* 3 Elegant Glass Feature Rows */}
          <div className="mt-3.5 space-y-2">
            {current.features.map((feat, idx) => {
              const FeatIcon = feat.icon;
              return (
                <div
                  key={idx}
                  className="flex items-center gap-3 p-2.5 rounded-[18px] bg-white/60 border border-white/85"
                >
                  <div className="w-9 h-9 rounded-xl bg-[#111318]/[0.05] text-[#111318] flex items-center justify-center shrink-0">
                    <FeatIcon className="w-4 h-4 text-[#E2532B]" strokeWidth={1.9} />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="text-[12.5px] font-bold text-[#111318] leading-snug">
                      {feat.title}
                    </div>
                    <div className="text-[11px] text-[#6E7582] truncate mt-0.5">
                      {feat.desc}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Bottom Glassmorphic Navigation Dock */}
      <div className="px-[20px] pt-2 pb-6 shrink-0 z-10">
        <div className="rounded-full glass-nav-pill p-2 pl-4 flex items-center justify-between gap-3">
          {/* Interactive Step Progress Dots */}
          <div className="flex items-center gap-1.5" role="tablist" aria-label="Onboarding steps">
            {screens.map((scr, idx) => {
              const isCurrent = idx === step;
              return (
                <button
                  key={scr.id}
                  type="button"
                  onClick={() => setStep(idx)}
                  aria-label={`Step ${idx + 1}`}
                  aria-current={isCurrent ? 'step' : undefined}
                  className={`h-2 rounded-full transition-all duration-300 ${
                    isCurrent
                      ? 'w-7 bg-[#E2532B]'
                      : idx < step
                      ? 'w-2 bg-[#111318]/45'
                      : 'w-2 bg-[#111318]/15 hover:bg-[#111318]/30'
                  }`}
                />
              );
            })}
          </div>

          {/* Back & Continue / Get Started Controls */}
          <div className="flex items-center gap-2">
            {step > 0 && (
              <button
                type="button"
                onClick={() => setStep((s) => s - 1)}
                className="min-h-[42px] px-3.5 rounded-full bg-[#111318]/[0.06] text-[#111318] hover:bg-[#111318]/[0.1] text-xs font-bold transition-all active:scale-95 flex items-center gap-1"
              >
                <ArrowLeft className={`w-3.5 h-3.5 ${isAr ? 'rotate-180' : ''}`} />
                <span>{isAr ? 'السابق' : 'Back'}</span>
              </button>
            )}

            {step < screens.length - 1 ? (
              <button
                type="button"
                onClick={() => setStep((s) => s + 1)}
                className="min-h-[42px] px-5 rounded-full bg-[#111318] text-white hover:bg-[#1e222b] text-xs font-bold transition-all active:scale-95 flex items-center gap-1.5 shadow-[0_8px_20px_-4px_rgba(17,19,24,0.28)]"
              >
                <span>{isAr ? 'التالي' : 'Continue'}</span>
                <ArrowRight className={`w-3.5 h-3.5 ${isAr ? 'rotate-180' : ''}`} />
              </button>
            ) : (
              <button
                type="button"
                onClick={onComplete}
                className="min-h-[42px] px-5 rounded-full bg-[#E2532B] text-white text-xs font-bold transition-all active:scale-95 flex items-center gap-1.5 shadow-[0_14px_34px_-8px_rgba(226,83,43,0.42)]"
              >
                <span>{isAr ? 'ابدأ الاستخدام الآن' : 'Get Started'}</span>
                <ArrowRight className={`w-3.5 h-3.5 ${isAr ? 'rotate-180' : ''}`} />
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
