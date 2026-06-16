# 規格交接:OriginCraft 自訂後綴稱號編輯器

## 1. 專案目標
做一個**線上稱號編輯器**,讓 Minecraft 玩家用**點擊**的方式搭配顏色與樣式,組出自己的「後綴稱號」。編輯完成後,網站產生**一條遊戲指令**,玩家複製貼回遊戲執行即套用。

- 網址(子網域):`https://tag.origincraft.tw`
- 站名建議:稱號工坊
- 輸出底層格式:**MiniMessage**(Adventure 的文字標記語言)

## 2. 玩家可使用的樣式(白名單,只有這些)
編輯器只能產生以下標籤,**其餘一律不可出現**(尤其禁止 `hover` / `click` / `insertion` / `font` / `key` / `translatable` 等互動或進階標籤)。

**顏色 — 16 個原版命名色:**
`black` `dark_blue` `dark_green` `dark_aqua` `dark_red` `dark_purple` `gold` `gray` `dark_gray` `blue` `green` `aqua` `red` `light_purple` `yellow` `white`

**顏色 — 進階:**
- Hex 顏色:`<#rrggbb>`(例 `<#ff8800>`)
- `<color:…>` / 縮寫 `<c:…>`(可接命名色或 hex,例 `<color:red>`、`<color:#ff8800>`)
- 漸層 `<gradient:…>`(可接 2 個以上顏色,例 `<gradient:gold:yellow>`、`<gradient:#ff0000:#00ff00:blue>`)
- 彩虹 `<rainbow>`

**樣式(及官方縮寫):**
- 粗體 `<bold>` / `<b>`
- 斜體 `<italic>` / `<i>` / `<em>`
- 底線 `<underlined>` / `<u>`
- 刪除線 `<strikethrough>` / `<st>`
- 亂碼 `<obfuscated>` / `<obf>`
- 重置 `<reset>` / `<r>`

> 對應的關閉標籤(`</bold>`、`</gradient>`、`</color>` …)允許使用。但**不可用 MiniMessage 的萬用關閉 `</>`**(會被伺服器判定為非法標籤)。靠字串結尾自動關閉,或用具名關閉標籤即可。

## 3. 硬性限制(超過就會被伺服器拒絕)
| 限制 | 數值 | 怎麼算 |
|------|------|--------|
| **原始字串長度** | ≤ **50** | 玩家實際輸入的整串 MiniMessage(含所有 `<…>` 標籤)的**字碼點(code point)**數,不是 byte、不是 UTF-16 char。 |
| **可見字數** | ≤ **10** | 把 MiniMessage 解析後、**去掉所有標籤**剩下的純文字,算**字碼點**數。中文字一個算 1、emoji 算 1。 |
| **不可有空白** | — | 整串(含可見文字內)**完全不能出現任何空白字元**(空格、Tab 等)。稱號文字本身也不能有空格。 |

## 4. 輸出格式(網站「複製指令」按鈕產生的字串)
```
/ts customsuffix <MiniMessage內容>
```
- `<MiniMessage內容>` 直接就是玩家組出來的那串(因為不含空格,所以是單一參數,不會被切斷)。
- 範例輸出:
  ```
  /ts customsuffix <gradient:gold:yellow>傳奇</gradient>
  /ts customsuffix <#ff0088><bold>櫻
  /ts customsuffix <rainbow>彩虹大師
  ```
- `ts` 是指令別名(全名 `tagsystem` 也可)。請固定用 `/ts customsuffix ` 當前綴。

## 5. 網站端驗證(請和伺服器端一字不差)
伺服器收到指令後會**再驗一次**(玩家可能不透過網站、手打指令),所以網站的檢查純粹是「友善的即時提示」,讓玩家在按複製前就知道有沒有問題。請依下列**完全相同的順序與規則**實作即時驗證:

1. **不可含任何空白字元** → 有就擋。
2. **原始長度** code point 數 ≤ 50。
3. **標籤白名單**:用 regex `/<[^>]+>/g` 掃出每個 `<…>`;取出標籤名(去掉 `<` `>`、去掉開頭的 `/`、只取第一個 `:` 之前的部分、轉小寫);
   - 若標籤名以 `#` 開頭(hex 顏色)→ 放行;
   - 否則必須在第 2 節的白名單內,不在就擋並指出是哪個標籤。
4. **MiniMessage 語法可被解析**(用只註冊「顏色 / 裝飾 / gradient / rainbow / reset」這幾類 resolver 的限制版解析器)。
5. **可見字數**:解析後取純文字,code point 數 ≤ 10,超過要顯示「目前幾字 / 上限 10 字」。

> 伺服器端用的是 Java Adventure MiniMessage,限制版解析器只開 `color()` / `decorations()` / `gradient()` / `rainbow()` / `reset()`。網站若用 JS,建議用官方對應的解析/或自行依上述白名單做等價檢查;**長度一律用 code point 計算**(JS 用 `[...str].length`,別用 `str.length`)。

## 6. 編輯器 UX 建議(非硬性)
- 即時預覽:把玩家組的稱號用 Minecraft 配色即時渲染出來(16 色 + hex + gradient + rainbow)。
- 兩個計數器:**原始 x/50**、**可見 x/10**,超標時變紅並擋住複製按鈕。
- 點擊式組裝:選文字 → 套用顏色/漸層/樣式,而不是讓玩家手打標籤。
- 阻止輸入空格(稱號文字框直接過濾空白)。
- 「複製指令」按鈕輸出第 4 節格式;旁邊放一行說明「複製後到遊戲貼上執行即可套用」。

## 7. 給編輯器的測試案例
| 輸入(可見文字 / 套用樣式) | 應輸出指令 | 驗證結果 |
|---|---|---|
| `傳奇`,gold→yellow 漸層 | `/ts customsuffix <gradient:gold:yellow>傳奇</gradient>` | ✅ 可見2、原始約36 |
| `彩虹大師`,rainbow | `/ts customsuffix <rainbow>彩虹大師` | ✅ |
| `這是一個非常長的稱號文字`(12字) | — | ❌ 可見字數 >10 |
| `傳 奇`(中間空格) | — | ❌ 含空白 |
| 用 `<hover:show_text:'hi'>X` | — | ❌ 非白名單標籤 hover |
