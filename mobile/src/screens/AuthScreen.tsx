import React, { useState } from 'react'
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
} from 'react-native'
import { useAuth } from '../context/AuthContext'
import { Input } from '../components/Input'
import { Button } from '../components/Button'

type Tab = 'signin' | 'signup'

export function AuthScreen() {
  const { signIn, signUp } = useAuth()
  const [tab, setTab] = useState<Tab>('signin')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit() {
    setError('')
    if (!email.trim() || !password.trim()) {
      setError('Email and password are required.')
      return
    }
    if (tab === 'signup' && !name.trim()) {
      setError('Name is required.')
      return
    }
    setLoading(true)
    try {
      if (tab === 'signin') {
        await signIn(email.trim(), password)
      } else {
        await signUp(name.trim(), email.trim(), password, phone.trim() || undefined)
      }
    } catch (e: any) {
      setError(e.message ?? 'Something went wrong.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <KeyboardAvoidingView
      style={styles.flex}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <ScrollView
        contentContainerStyle={styles.container}
        keyboardShouldPersistTaps="handled"
      >
        {/* Logo / brand */}
        <View style={styles.brand}>
          <Text style={styles.logo}>N</Text>
          <Text style={styles.brandName}>Nexa</Text>
          <Text style={styles.tagline}>Your AI message hub</Text>
        </View>

        {/* Tab switcher */}
        <View style={styles.tabs}>
          <TouchableOpacity
            style={[styles.tab, tab === 'signin' && styles.tabActive]}
            onPress={() => { setTab('signin'); setError('') }}
          >
            <Text style={[styles.tabText, tab === 'signin' && styles.tabTextActive]}>
              Sign in
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.tab, tab === 'signup' && styles.tabActive]}
            onPress={() => { setTab('signup'); setError('') }}
          >
            <Text style={[styles.tabText, tab === 'signup' && styles.tabTextActive]}>
              Sign up
            </Text>
          </TouchableOpacity>
        </View>

        {/* Form */}
        <View style={styles.form}>
          {tab === 'signup' && (
            <>
              <Input
                label="Name"
                placeholder="Your name"
                autoCapitalize="words"
                value={name}
                onChangeText={setName}
              />
              <Input
                label="Phone number (optional)"
                placeholder="+1 234 567 8900"
                keyboardType="phone-pad"
                autoComplete="tel"
                value={phone}
                onChangeText={setPhone}
              />
            </>
          )}
          <Input
            label="Email"
            placeholder="you@example.com"
            autoCapitalize="none"
            keyboardType="email-address"
            autoComplete="email"
            value={email}
            onChangeText={setEmail}
          />
          <Input
            label="Password"
            placeholder="••••••••"
            secureTextEntry
            autoComplete={tab === 'signin' ? 'current-password' : 'new-password'}
            value={password}
            onChangeText={setPassword}
          />

          {error ? <Text style={styles.error}>{error}</Text> : null}

          <Button
            title={tab === 'signin' ? 'Sign in' : 'Create account'}
            onPress={handleSubmit}
            loading={loading}
            style={styles.submitBtn}
          />
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  )
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: '#0f172a' },
  container: {
    flexGrow: 1,
    justifyContent: 'center',
    paddingHorizontal: 24,
    paddingVertical: 40,
  },
  brand: { alignItems: 'center', marginBottom: 40 },
  logo: {
    width: 64,
    height: 64,
    borderRadius: 18,
    backgroundColor: '#6366f1',
    color: '#fff',
    fontSize: 32,
    fontWeight: '700',
    textAlign: 'center',
    lineHeight: 64,
    overflow: 'hidden',
  },
  brandName: {
    color: '#f9fafb',
    fontSize: 28,
    fontWeight: '700',
    marginTop: 12,
  },
  tagline: { color: '#6b7280', fontSize: 14, marginTop: 4 },
  tabs: {
    flexDirection: 'row',
    backgroundColor: '#1f2937',
    borderRadius: 12,
    padding: 4,
    marginBottom: 28,
  },
  tab: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 9,
    alignItems: 'center',
  },
  tabActive: { backgroundColor: '#374151' },
  tabText: { color: '#6b7280', fontWeight: '600', fontSize: 14 },
  tabTextActive: { color: '#f9fafb' },
  form: {},
  error: {
    color: '#f87171',
    fontSize: 13,
    marginBottom: 12,
    textAlign: 'center',
  },
  submitBtn: { marginTop: 4 },
})
