export type Language = 'en' | 'ar';

export interface Translations {
  appName: string;
  campaignsTitle: string;
  campaignsCount: (count: number, contacts: number) => string;
  newCampaign: string;
  searchPlaceholder: string;
  filterAll: string;
  filterReady: string;
  filterActive: string;
  filterCompleted: string;
  filterPaused: string;
  filterRunning: string;

  // Stats
  statTotalCampaigns: string;
  statTotalContacts: string;
  statCompletedCalls: string;
  statPendingCalls: string;
  statProgress: string;

  // Card elements
  contactsCount: (count: number) => string;
  completedOfTotal: (completed: number, total: number) => string;
  remainingCalls: (count: number) => string;
  lastActive: string;
  statusReady: string;
  statusActive: string;
  statusPaused: string;
  statusCompleted: string;
  statusRunning: string;

  // Actions
  actionEdit: string;
  actionRename: string;
  actionResume: string;
  actionPause: string;
  actionDelete: string;
  actionShare: string;
  actionReset: string;
  actionRemoveDuplicates: string;
  actionViewResults: string;
  actionSelect: string;
  actionCancel: string;
  actionSave: string;
  actionCreate: string;
  actionStartCalling: string;
  actionSimulateCall: string;

  // Multi-selection
  selectedCount: (count: number) => string;
  selectAll: string;
  deselectAll: string;
  bulkDelete: string;
  bulkPause: string;
  bulkResume: string;
  bulkShare: string;

  // Empty state
  emptyTitle: string;
  emptySubtitle: string;
  emptyAction: string;
  noSearchResultsTitle: string;
  noSearchResultsSubtitle: string;
  clearSearch: string;

  // Bottom Nav (5 Principal Destinations: Dialer, Results, Campaigns, History, Settings)
  navKeypad: string;
  navResults: string;
  navCampaigns: string;
  navHistory: string;
  navSettings: string;

  // Modals & Details
  campaignDetails: string;
  performanceMetrics: string;
  answeredRate: string;
  failedRate: string;
  avgDuration: string;
  dialerMode: string;
  delaySec: string;
  contactList: string;
  confirmDeleteTitle: string;
  confirmDeleteMsg: (name: string) => string;
  confirmBulkDeleteMsg: (count: number) => string;
  duplicatesCleanedMsg: (count: number) => string;
  progressResetMsg: string;

  // Sort
  sortBy: string;
  sortRecent: string;
  sortName: string;
  sortProgress: string;
  sortContacts: string;
  sortCreated: string;

  // Live Dialer strings
  dialerTitle: string;
  currentlyCalling: string;
  nextCallIn: (seconds: number) => string;
  callingPhaseDialing: string;
  callingPhaseRinging: string;
  callingPhaseConnected: string;
  sessionStatusIdle: string;
  sessionStatusReady: string;
  sessionStatusStarting: string;
  sessionStatusRunning: string;
  sessionStatusCalling: string;
  sessionStatusWaiting: string;
  sessionStatusAwaitingResult: string;
  sessionStatusPaused: string;
  sessionStatusFinished: string;
  sessionStatusStopped: string;
  sessionStatusError: string;
  actionStartSession: string;
  actionResumeSession: string;
  actionRestartCampaign: string;
  actionRestartBeginning: string;
  actionPauseSession: string;
  actionSkipCall: string;
  actionStopSession: string;
  actionConfirmStop: string;
  stopSessionConfirmTitle: string;
  stopSessionConfirmMsg: string;
  upNext: string;
  upNextRemaining: (count: number) => string;
  removeFromQueue: string;
  sessionConfig: string;
  retryCount: string;
  audioRoute: string;
  readyToCallHeading: string;
  readyToCallSubtitle: string;
  sessionCompletedTitle: string;
  sessionCompletedSubtitle: string;
  chooseAnotherCampaign: string;
  outcomeAnswered: string;
  outcomeNoAnswer: string;
  outcomeFailed: string;
  outcomeUnverified: string;
  outcomeSkipped: string;

  // Call Result Stages
  stagePotential: string;
  stageNotInterested: string;
  stageCallBack: string;
  stageWhatsapp: string;
  stageVoicemail: string;
  stageNone: string;
}

export const translations: Record<Language, Translations> = {
  en: {
    appName: 'O2Dialer',
    campaignsTitle: 'Campaigns',
    campaignsCount: (count, contacts) =>
      `${count} ${count === 1 ? 'campaign' : 'campaigns'} · ${contacts.toLocaleString()} contacts`,
    newCampaign: '+ New Campaign',
    searchPlaceholder: 'Search campaigns',
    filterAll: 'All',
    filterReady: 'Ready',
    filterActive: 'Active',
    filterCompleted: 'Completed',
    filterPaused: 'Paused',
    filterRunning: 'Running',

    statTotalCampaigns: 'Campaigns',
    statTotalContacts: 'Contacts',
    statCompletedCalls: 'Called',
    statPendingCalls: 'Remaining',
    statProgress: 'Overall Progress',

    contactsCount: (count) => `${count.toLocaleString()} contacts`,
    completedOfTotal: (completed, total) => `${completed.toLocaleString()} / ${total.toLocaleString()}`,
    remainingCalls: (count) => `${count.toLocaleString()} remaining`,
    lastActive: 'Active',
    statusReady: 'Ready',
    statusActive: 'Active',
    statusPaused: 'Paused',
    statusCompleted: 'Completed',
    statusRunning: 'Running',

    actionEdit: 'Edit Campaign',
    actionRename: 'Rename',
    actionResume: 'Start Calling',
    actionPause: 'Pause',
    actionDelete: 'Delete',
    actionShare: 'Share Report',
    actionReset: 'Reset Progress',
    actionRemoveDuplicates: 'Remove Duplicates',
    actionViewResults: 'View Results',
    actionSelect: 'Select',
    actionCancel: 'Cancel',
    actionSave: 'Save Changes',
    actionCreate: 'Create Campaign',
    actionStartCalling: 'Start Auto-Dialer',
    actionSimulateCall: 'Simulate Next Call',

    selectedCount: (count) => `${count} selected`,
    selectAll: 'Select All',
    deselectAll: 'Deselect',
    bulkDelete: 'Delete Selected',
    bulkPause: 'Pause Selected',
    bulkResume: 'Resume Selected',
    bulkShare: 'Export Selected',

    emptyTitle: 'No campaigns yet',
    emptySubtitle: 'Create your first calling campaign to organize contact lists and automate outbound calls.',
    emptyAction: 'Create Campaign',
    noSearchResultsTitle: 'No matching campaigns',
    noSearchResultsSubtitle: 'Try adjusting your search query or clearing status filters.',
    clearSearch: 'Clear Search',

    navKeypad: 'Dialer',
    navResults: 'Results',
    navCampaigns: 'Campaigns',
    navHistory: 'History',
    navSettings: 'Settings',

    campaignDetails: 'Campaign Details',
    performanceMetrics: 'Performance Metrics',
    answeredRate: 'Answered Rate',
    failedRate: 'Unreachable',
    avgDuration: 'Avg. Duration',
    dialerMode: 'Outgoing SIM',
    delaySec: 'Call Interval',
    contactList: 'Campaign Contacts',
    confirmDeleteTitle: 'Delete Campaign',
    confirmDeleteMsg: (name) => `Are you sure you want to delete "${name}"? This action cannot be undone.`,
    confirmBulkDeleteMsg: (count) => `Are you sure you want to delete ${count} campaigns? Associated call progress will be permanently removed.`,
    duplicatesCleanedMsg: (count) => `${count} duplicate ${count === 1 ? 'contact' : 'contacts'} removed successfully.`,
    progressResetMsg: 'Campaign progress reset to 0%. All contacts returned to pending.',

    sortBy: 'Sort by',
    sortRecent: 'Recent Activity',
    sortName: 'Campaign Name',
    sortProgress: 'Progress',
    sortContacts: 'Contacts Count',
    sortCreated: 'Date Created',

    dialerTitle: 'Dialer',
    currentlyCalling: 'Current Contact',
    nextCallIn: (seconds) => `Next call in 00:${seconds < 10 ? `0${seconds}` : seconds}`,
    callingPhaseDialing: 'Calling…',
    callingPhaseRinging: 'Ringing…',
    callingPhaseConnected: 'In Call',
    sessionStatusIdle: 'Idle',
    sessionStatusReady: 'Ready',
    sessionStatusStarting: 'Starting',
    sessionStatusRunning: 'Running',
    sessionStatusCalling: 'Calling',
    sessionStatusWaiting: 'Waiting',
    sessionStatusAwaitingResult: 'Awaiting Result',
    sessionStatusPaused: 'Paused',
    sessionStatusFinished: 'Finished',
    sessionStatusStopped: 'Stopped',
    sessionStatusError: 'Error',
    actionStartSession: 'Start Campaign',
    actionResumeSession: 'Resume Campaign',
    actionRestartCampaign: 'Restart Campaign',
    actionRestartBeginning: 'Restart from Beginning',
    actionPauseSession: 'Pause Session',
    actionSkipCall: 'Skip Call',
    actionStopSession: 'Stop Session',
    actionConfirmStop: 'Stop Calling',
    stopSessionConfirmTitle: 'Stop Calling Session',
    stopSessionConfirmMsg: 'Are you sure you want to stop this automated session? All completed calls and saved notes will be preserved.',
    upNext: 'Up Next',
    upNextRemaining: (count) => `${count} remaining`,
    removeFromQueue: 'Remove from Queue',
    sessionConfig: 'Session Configuration',
    retryCount: '2 retries',
    audioRoute: 'Speakerphone',
    readyToCallHeading: 'Ready to Call',
    readyToCallSubtitle: 'Launch an automated outbound calling session through your Android SIM account.',
    sessionCompletedTitle: 'Session Completed',
    sessionCompletedSubtitle: 'All queued contacts in this campaign have been processed.',
    chooseAnotherCampaign: 'Choose Another Campaign',
    outcomeAnswered: 'Answered',
    outcomeNoAnswer: 'No Answer',
    outcomeFailed: 'Failed',
    outcomeUnverified: 'Unverified',
    outcomeSkipped: 'Skipped',

    stagePotential: 'Potential',
    stageNotInterested: 'Not Interested',
    stageCallBack: 'Call Back',
    stageWhatsapp: 'WhatsApp',
    stageVoicemail: 'Voicemail',
    stageNone: 'Unclassified',
  },
  ar: {
    appName: 'أو تو دايلر',
    campaignsTitle: 'الحملات',
    campaignsCount: (count, contacts) =>
      `${count} ${count === 1 ? 'حملة' : 'حملات'} · ${contacts.toLocaleString('ar-EG')} جهة اتصال`,
    newCampaign: '+ حملة جديدة',
    searchPlaceholder: 'البحث في الحملات',
    filterAll: 'الكل',
    filterReady: 'جاهزة',
    filterActive: 'نشطة',
    filterCompleted: 'مكتملة',
    filterPaused: 'متوقفة',
    filterRunning: 'جارية',

    statTotalCampaigns: 'الحملات',
    statTotalContacts: 'جهات الاتصال',
    statCompletedCalls: 'تم الاتصال',
    statPendingCalls: 'المتبقية',
    statProgress: 'التقدم الكلي',

    contactsCount: (count) => `${count.toLocaleString('ar-EG')} جهة اتصال`,
    completedOfTotal: (completed, total) => `${completed.toLocaleString('ar-EG')} / ${total.toLocaleString('ar-EG')}`,
    remainingCalls: (count) => `${count.toLocaleString('ar-EG')} متبقية`,
    lastActive: 'النشاط',
    statusReady: 'جاهزة',
    statusActive: 'نشطة',
    statusPaused: 'متوقفة',
    statusCompleted: 'مكتملة',
    statusRunning: 'قيد التشغيل',

    actionEdit: 'تعديل الحملة',
    actionRename: 'إعادة التسمية',
    actionResume: 'بدء الاتصال',
    actionPause: 'إيقاف مؤقت',
    actionDelete: 'حذف',
    actionShare: 'مشاركة التقرير',
    actionReset: 'إعادة ضبط التقدم',
    actionRemoveDuplicates: 'إزالة التكرار',
    actionViewResults: 'عرض النتائج',
    actionSelect: 'تحديد',
    actionCancel: 'إلغاء',
    actionSave: 'حفظ التعديلات',
    actionCreate: 'إنشاء حملة',
    actionStartCalling: 'بدء الاتصال الآلي',
    actionSimulateCall: 'محاكاة المكالمة التالية',

    selectedCount: (count) => `تم تحديد ${count}`,
    selectAll: 'تحديد الكل',
    deselectAll: 'إلغاء التحديد',
    bulkDelete: 'حذف المحدد',
    bulkPause: 'إيقاف المحدد',
    bulkResume: 'استئناف المحدد',
    bulkShare: 'تصدير المحدد',

    emptyTitle: 'لا توجد حملات حتى الآن',
    emptySubtitle: 'أنشئ أول حملة اتصال آلي لتنظيم قوائم جهات الاتصال وإجراء المكالمات بكفاءة.',
    emptyAction: 'إنشاء حملة',
    noSearchResultsTitle: 'لم يتم العثور على نتائج',
    noSearchResultsSubtitle: 'حاول تغيير عبارة البحث أو إعادة تعيين التصفية.',
    clearSearch: 'مسح البحث',

    navKeypad: 'المتصل',
    navResults: 'النتائج',
    navCampaigns: 'الحملات',
    navHistory: 'السجل',
    navSettings: 'الإعدادات',

    campaignDetails: 'تفاصيل الحملة',
    performanceMetrics: 'مؤشرات الأداء',
    answeredRate: 'نسبة الرد',
    failedRate: 'تعذر الاتصال',
    avgDuration: 'متوسط المدة',
    dialerMode: 'شريحة الاتصال',
    delaySec: 'الفاصل الزمني',
    contactList: 'جهات اتصال الحملة',
    confirmDeleteTitle: 'حذف الحملة',
    confirmDeleteMsg: (name) => `هل أنت متأكد من حذف "${name}"؟ لا يمكن التراجع عن هذا الإجراء.`,
    confirmBulkDeleteMsg: (count) => `هل أنت متأكد من حذف ${count} حملات؟ سيتم حذف سجل التقدم نهائياً.`,
    duplicatesCleanedMsg: (count) => `تمت إزالة ${count} من جهات الاتصال المكررة بنجاح.`,
    progressResetMsg: 'تمت إعادة ضبط التقدم إلى 0%. جميع جهات الاتصال أصبحت معلقة.',

    sortBy: 'ترتيب حسب',
    sortRecent: 'آخر نشاط',
    sortName: 'اسم الحملة',
    sortProgress: 'نسبة الإنجاز',
    sortContacts: 'عدد جهات الاتصال',
    sortCreated: 'تاريخ الإنشاء',

    dialerTitle: 'المتصل الآلي',
    currentlyCalling: 'جهة الاتصال الحالية',
    nextCallIn: (seconds) => `المكالمة التالية خلال 00:${seconds < 10 ? `0${seconds}` : seconds}`,
    callingPhaseDialing: 'جارِ الاتصال…',
    callingPhaseRinging: 'يرن…',
    callingPhaseConnected: 'متصل الآن',
    sessionStatusIdle: 'خامل',
    sessionStatusReady: 'جاهز',
    sessionStatusStarting: 'بدء العد',
    sessionStatusRunning: 'نشط',
    sessionStatusCalling: 'يتصل',
    sessionStatusWaiting: 'انتظار',
    sessionStatusAwaitingResult: 'بانتظار التصنيف',
    sessionStatusPaused: 'متوقف مؤقتاً',
    sessionStatusFinished: 'مكتمل',
    sessionStatusStopped: 'تم الإيقاف',
    sessionStatusError: 'خطأ',
    actionStartSession: 'بدء الحملة',
    actionResumeSession: 'استئناف الحملة',
    actionRestartCampaign: 'إعادة تشغيل الحملة',
    actionRestartBeginning: 'البدء من البداية',
    actionPauseSession: 'إيقاف الجلسة مؤقتاً',
    actionSkipCall: 'تخطي الرقم',
    actionStopSession: 'إنهاء الجلسة',
    actionConfirmStop: 'إنهاء الاتصال',
    stopSessionConfirmTitle: 'إنهاء جلسة الاتصال',
    stopSessionConfirmMsg: 'هل أنت متأكد من إنهاء جلسة الاتصال الآلي؟ سيتم حفظ جميع المكالمات والملاحظات المسجلة.',
    upNext: 'التالي في القائمة',
    upNextRemaining: (count) => `${count} متبقية`,
    removeFromQueue: 'إزالة من قائمة الانتظار',
    sessionConfig: 'إعدادات الجلسة',
    retryCount: 'محاولتان',
    audioRoute: 'مكبر الصوت',
    readyToCallHeading: 'جاهز للاتصال',
    readyToCallSubtitle: 'أطلق جلسة اتصال آلي منتظمة عبر شريحة الهاتف المختارة.',
    sessionCompletedTitle: 'اكتملت الجلسة بنجاح',
    sessionCompletedSubtitle: 'تمت معالجة جميع جهات الاتصال المجدولة في هذه الحملة.',
    chooseAnotherCampaign: 'اختيار حملة أخرى',
    outcomeAnswered: 'تم الرد',
    outcomeNoAnswer: 'لا يوجد رد',
    outcomeFailed: 'فشل',
    outcomeUnverified: 'غير مؤكد',
    outcomeSkipped: 'تم التخطي',

    stagePotential: 'عميل محتمل',
    stageNotInterested: 'غير مهتم',
    stageCallBack: 'معاودة اتصال',
    stageWhatsapp: 'واتساب',
    stageVoicemail: 'بريد صوتي',
    stageNone: 'غير مصنف',
  },
};
