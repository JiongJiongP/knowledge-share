# Homepage Stats Dashboard Design

**Date:** 2026-05-27
**Status:** Approved

## Goal

Add a compact stats overview section to the homepage, above the existing content feed. The stats should provide a quick resource overview without turning the homepage into a full dashboard.

## Decisions

| Aspect | Choice | Rationale |
|--------|--------|-----------|
| Layout | Top banner row, above content list | Clean, minimal, doesn't compete with content |
| Stats | 5 cards: total content, total users, groups, today's content, total comments | Content-focused metrics that show platform health |
| Visual style | Colorful gradient backgrounds + white text | Lively and distinctive, stands out from the content list |
| Responsive | Wrap to 2 columns below 768px | Keeps cards readable on narrow screens |
| Interactivity | Cards are display-only, no click targets | Keep it simple; links to detail pages would distract |
| Number format | `>= 10000` → `XX.X万`; `< 10000` → comma-separated | Chinese-friendly large-number formatting |

## Page Layout

```
┌────────────────── Stat Cards (5-col row) ────────────────┐
│  [gradient]   [gradient]   [gradient]   [gradient]   [gradient]  │
│   📄 50.0万     👥 1,000     👥 30       🔥 128      💬 200.0万  │
│   总内容         总用户        群组数       今日发布      总评论      │
├────────────────── Filter Bar ────────────────────────────┤
│  [type▼] [group▼] [sort▼]  [tags...]                     │
├────────────────── Content List ──────────────────────────┤
│  Title 1                                   2h ago        │
│  Title 2                                   4h ago        │
│  ...                                                     │
├────────────────── Pagination ────────────────────────────┤
└──────────────────────────────────────────────────────────┘
```

## Backend API

New public endpoint — existing analytics endpoints are admin-only (`/api/admin/analytics`).

```
GET /api/stats/overview

Response 200:
{
  "code": 200,
  "data": {
    "totalContents": 500000,
    "totalUsers": 1000,
    "totalGroups": 30,
    "todayContents": 128,
    "totalComments": 2000000
  }
}
```

### Data Sources

| Field | Source | Query |
|-------|--------|-------|
| totalContents | ContentMapper | `selectCount(null)` on knowledge_content where is_deleted=0 |
| totalUsers | UserMapper | `selectCount(null)` on user |
| totalGroups | GroupMapper | `selectCount(null)` on group_info where visibility='PUBLIC' |
| todayContents | ContentMapper | `selectCount` with `ge(createdAt, today)` |
| totalComments | CommentMapper | `selectCount(null)` on comment |

## Frontend Changes

### New file

- `frontend/src/api/stats.js` — API wrapper for `GET /api/stats/overview`

### Modified files

- `frontend/src/views/HomePage.vue` — add stat cards row above filter bar, fetch stats on mount

### Component structure

The stat cards are rendered inline in HomePage.vue (no separate component — 5 cards is simple enough to not warrant extraction).

Each card is a `<div>` with:
- Gradient background via CSS `linear-gradient`
- Emoji icon, large number, label text
- 5 unique gradient color schemes

## Backend Changes

### New file

- `knowledge-web/src/main/java/com/company/web/controller/StatsController.java`

### Implementation notes

- Controller is in `knowledge-web` module (has access to all sub-module mappers)
- Use constructor injection for the 5 mappers
- No service layer needed — simple count queries are fine inline
- Return `Result<Map<String, Object>>`

## Edges Cases

- **Empty database**: All counts return 0, cards show "0"
- **Missing mapper**: Spring fails fast at startup with clear error
- **Slow queries**: Each `selectCount` is a single `SELECT COUNT(*)`; 5 such queries per page load is acceptable. If perf becomes a concern, add caching later
- **Number overflow**: Long values (up to 2^63) are far beyond realistic counts

## Out of Scope

- Click-through to detail pages from stat cards
- Real-time updates / WebSocket
- Historical trend data (charts)
- Admin-only metrics (audit queue, sensitive words)
- Caching layer
