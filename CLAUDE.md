# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working in this repository.

Whole30 Journal is a Kotlin Multiplatform app for Android and iOS: shared Compose Multiplatform
screens and shared ViewModels in `shared/`, with `androidApp` and `iosApp` owning only navigation
and DI bootstrap.

**Whenever this file is read, also read [ARCHITECTURE.md](ARCHITECTURE.md) and [AGENTS.md](AGENTS.md)
in full before doing anything else.** Together they are the authoritative source for this
project's architecture, conventions, build commands, and working rules — this file only exists to
point to them.
