import React from 'react'
import { View, Text, StyleSheet } from 'react-native'
import { useAuth } from '../context/AuthContext'
import { Button } from '../components/Button'

export function HomeScreen() {
  const { user, signOut } = useAuth()

  return (
    <View style={styles.container}>
      <Text style={styles.greeting}>Welcome, {user?.name ?? 'there'}</Text>
      <Text style={styles.sub}>{user?.email}</Text>
      <Button
        title="Sign out"
        variant="ghost"
        onPress={signOut}
        style={styles.btn}
      />
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0f172a',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  greeting: { color: '#f9fafb', fontSize: 24, fontWeight: '700' },
  sub: { color: '#6b7280', fontSize: 14, marginTop: 6 },
  btn: { marginTop: 32, width: 160 },
})
