# 🎨 LuxDine Restaurant – Design System

This design system outlines the visual and functional standards for LuxDine web application UI. It ensures consistency across all pages and components.

---

## 📦 1. Foundation (Nền tảng cơ bản)

### ✅ Color Palette

| Name                | Color Code         | Usage                     |
|---------------------|--------------------|----------------------------|
| Primary             | `#0A0A23`          | Buttons, headings          |
| Secondary           | `#F97316`          | CTA buttons (e.g., Reserve)|
| Neutral Background  | `#F9FAFB`          | Form backgrounds           |
| Border Gray         | `#E5E7EB`          | Input and card borders     |
| Text Gray           | `#6B7280`          | Secondary text             |
| Success             | `#22C55E`          | Vegetarian indicators      |
| Warning             | `#FACC15`          | Allergy/alert boxes        |
| Error               | `#EF4444`          | Input validation errors    |

### ✅ Typography

| Type        | Size  | Weight     | Line Height | Use Case                   |
|-------------|-------|------------|-------------|-----------------------------|
| Heading 1   | 32px  | Bold       | 1.2         | Hero banners, main titles  |
| Heading 2   | 24px  | Semi-Bold  | 1.3         | Section headers            |
| Heading 3   | 20px  | Medium     | 1.4         | Cards, subheadings         |
| Body Text   | 16px  | Normal     | 1.5         | General text content       |
| Small Text  | 14px  | Normal     | 1.4         | Notes, captions            |

Font: `Inter`, `Poppins`, or `system-ui, sans-serif`

---

## 🧱 2. Components

### 🔘 Buttons

| Variant     | Style                                  | Use Case              |
|-------------|-----------------------------------------|------------------------|
| Primary     | Dark navy background, white text        | Sign In, Create, Order |
| Secondary   | White background, orange border/text    | Reserve, Order Now     |
| Disabled    | Light gray background, gray text        | Inactive buttons       |

- Border Radius: 8px
- Padding: 10px 24px

### 📝 Input Fields

- Border: `1px solid #E5E7EB`
- Focused: `border-color: #0A0A23`
- Placeholder: `#9CA3AF`
- Border Radius: 6px
- Validation:
  - Success: `#22C55E`
  - Error: `#EF4444`

### 🧾 Tabs

- Background: `#F3F4F6`
- Active Tab: White background, bold text, underline
- Inactive Tab: Gray text

### 📦 Cards

Used for Menu items, Table selections, Waitlist entries

- Border: `1px solid #E5E7EB`
- Border-radius: 12px
- Padding: 16px
- Optional shadow: `0 1px 2px rgba(0,0,0,0.05)`

---

## 🗂 3. Layout

### Grid

- 12-column layout
- Max width: 1200px
- Gutter: 16px or 24px
- Section margin: 40–64px

### Spacing Tokens

| Token | Size |
|-------|------|
| xs    | 4px  |
| sm    | 8px  |
| md    | 16px |
| lg    | 24px |
| xl    | 32px |
| xxl   | 64px |

---

## 💬 4. UX Elements

### Alert Boxes

- **Allergy Info:** Background `#FEF3C7`, border orange
- **Info Note:** Light gray border, padding 16px

---

## 🧭 5. Navigation

Top navigation bar:

- Left: Logo
- Right: Home, Menu, Reservations, Order, Contact, Login

Hover: underline or slight drop shadow

---

## 📲 6. Responsiveness

### Breakpoints

- `sm` < 640px
- `md` ≥ 640px
- `lg` ≥ 768px
- `xl` ≥ 1024px

Stacked elements on small screens, scrollable pills for menu filter.

---

## ✅ 7. Naming Convention (Atomic Design)

| Level     | Name            | Description                 |
|-----------|-----------------|-----------------------------|
| Atom      | `ButtonPrimary` | Base button                 |
| Atom      | `InputField`    | Text input field            |
| Molecule  | `LoginForm`     | Grouped login components    |
| Molecule  | `MenuCard`      | Dish display card           |
| Organism  | `Navbar`        | Top navigation bar          |
| Page      | `MenuPage`      | Full menu listing           |

---

## 📦 Design Tokens (JSON Style)

```json
{
  "colors": {
    "primary": "#0A0A23",
    "secondary": "#F97316",
    "text": "#111827",
    "gray": "#6B7280"
  },
  "font": {
    "body": "Inter, sans-serif",
    "sizeBase": "16px",
    "sizeHeading": "32px"
  },
  "borderRadius": "8px",
  "spacing": {
    "sm": "8px",
    "md": "16px",
    "lg": "32px"
  }
}
```