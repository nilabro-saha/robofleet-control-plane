# Fleet Dashboard UI Module

## Intent

This module is the **operator visibility surface** of the MVP.

It deliberately stays lightweight (plain HTML/CSS/JS) to keep attention on the behavior of the system rather than framework setup.

## Why this exists in the MVP

The control plane is only useful if humans can quickly understand fleet health.
This UI converts API responses into an at-a-glance operations view.

## Responsibilities

- Poll `GET /api/robot-statuses` and render current robot state
- Offer operator controls for:
  - live refresh on/off
  - refresh interval selection
  - manual refresh
- Support backend-driven sorting from the table headers:
  - click a header to sort ascending by that field
  - click the same header again to toggle descending
- Open a right-side details panel when a robot row is clicked
  - fetches single-robot data from `GET /api/robot-statuses/{id}`
- Present timestamps in browser-local time for operator context

## Runtime assumption

The API base URL is provided by query parameter (for example):

`/?apiBase=http://localhost:8081`

This keeps the UI static and backend-agnostic.

## Sorting contract

Sorting is not performed in the browser. The UI calls backend pageable sort params:

- `GET /api/robot-statuses?sort=<field>,<direction>&size=100`

Supported sort fields used by the UI:

- `robotId`
- `displayName`
- `lifecycleStatus`
- `positionX`
- `positionY`
- `battery`
- `status`
- `timestamp`
