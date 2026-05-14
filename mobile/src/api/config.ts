import { Platform } from 'react-native'

// Switch between local dev and production via the ENV variable.
// In app.config.ts, set extra.apiUrl to the desired value per build profile.
// Fallback: emulator localhost (10.0.2.2) for Android, localhost for iOS.
const DEFAULT_LOCAL = Platform.OS === 'android'
  ? 'http://10.0.2.2:8080'
  : 'http://localhost:8080'

// Set EXPO_PUBLIC_API_URL in .env.local for local dev
// or in eas.json build profile for production
export const BASE_URL: string =
  process.env.EXPO_PUBLIC_API_URL ?? DEFAULT_LOCAL
