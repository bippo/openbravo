// ** I18N

// Calendar RU language
// Translation: Sly Golovanov, http://golovanov.net, <sly@golovanov.net>
// Encoding: any
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// full day names
Calendar._DN = new Array
("âîñêðåñåíüå",
 "ïîíåäåëüíèê",
 "âòîðíèê",
 "ñðåäà",
 "÷åòâåðã",
 "ïÿòíèöà",
 "ñóááîòà",
 "âîñêðåñåíüå");

// Please note that the following array of short day names (and the same goes
// for short month names, _SMN) isn't absolutely necessary.  We give it here
// for exemplification on how one can customize the short day names, but if
// they are simply the first N letters of the full name you can simply say:
//
//   Calendar._SDN_len = N; // short day name length
//   Calendar._SMN_len = N; // short month name length
//
// If N = 3 then this is not needed either since we assume a value of 3 if not
// present, to be compatible with translation files that were written before
// this feature.

// short day names
Calendar._SDN = new Array
("âñê",
 "ïîí",
 "âòð",
 "ñðä",
 "÷åò",
 "ïÿò",
 "ñóá",
 "âñê");

// full month names
Calendar._MN = new Array
("ÿíâàðü",
 "ôåâðàëü",
 "ìàðò",
 "àïðåëü",
 "ìàé",
 "èþíü",
 "èþëü",
 "àâãóñò",
 "ñåíòÿáðü",
 "îêòÿáðü",
 "íîÿáðü",
 "äåêàáðü");

// short month names
Calendar._SMN = new Array
("ÿíâ",
 "ôåâ",
 "ìàð",
 "àïð",
 "ìàé",
 "èþí",
 "èþë",
 "àâã",
 "ñåí",
 "îêò",
 "íîÿ",
 "äåê");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Î êàëåíäàðå...";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2005 / Author: Mihai Bazon\n" + // don't translate this this ;-)
"For latest version visit: http://www.dynarch.com/projects/calendar/\n" +
"Distributed under GNU LGPL.  See http://gnu.org/licenses/lgpl.html for details." +
"\n\n" +
"Êàê âûáðàòü äàòó:\n" +
"- Ïðè ïîìîùè êíîïîê \xab, \xbb ìîæíî âûáðàòü ãîä\n" +
"- Ïðè ïîìîùè êíîïîê " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " ìîæíî âûáðàòü ìåñÿö\n" +
"- Ïîäåðæèòå ýòè êíîïêè íàæàòûìè, ÷òîáû ïîÿâèëîñü ìåíþ áûñòðîãî âûáîðà.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Êàê âûáðàòü âðåìÿ:\n" +
"- Ïðè êëèêå íà ÷àñàõ èëè ìèíóòàõ îíè óâåëè÷èâàþòñÿ\n" +
"- ïðè êëèêå ñ íàæàòîé êëàâèøåé Shift îíè óìåíüøàþòñÿ\n" +
"- åñëè íàæàòü è äâèãàòü ìûøêîé âëåâî/âïðàâî, îíè áóäóò ìåíÿòüñÿ áûñòðåå.";

Calendar._TT["PREV_YEAR"] = "Íà ãîä íàçàä (óäåðæèâàòü äëÿ ìåíþ)";
Calendar._TT["PREV_MONTH"] = "Íà ìåñÿö íàçàä (óäåðæèâàòü äëÿ ìåíþ)";
Calendar._TT["GO_TODAY"] = "Ñåãîäíÿ";
Calendar._TT["NEXT_MONTH"] = "Íà ìåñÿö âïåðåä (óäåðæèâàòü äëÿ ìåíþ)";
Calendar._TT["NEXT_YEAR"] = "Íà ãîä âïåðåä (óäåðæèâàòü äëÿ ìåíþ)";
Calendar._TT["SEL_DATE"] = "Âûáåðèòå äàòó";
Calendar._TT["DRAG_TO_MOVE"] = "Ïåðåòàñêèâàéòå ìûøêîé";
Calendar._TT["PART_TODAY"] = " (ñåãîäíÿ)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Ïåðâûé äåíü íåäåëè áóäåò %s";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "Çàêðûòü";
Calendar._TT["TODAY"] = "Ñåãîäíÿ";
Calendar._TT["TIME_PART"] = "(Shift-)êëèê èëè íàæàòü è äâèãàòü";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%e %b, %a";

Calendar._TT["WK"] = "íåä";
Calendar._TT["TIME"] = "Âðåìÿ:";
