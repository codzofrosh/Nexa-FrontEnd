import React from 'react'
import {
  TextInput,
  TextInputProps,
  Text,
  View,
  StyleSheet,
} from 'react-native'

interface Props extends TextInputProps {
  label: string
  error?: string
}

export function Input({ label, error, style, ...props }: Props) {
  return (
    <View style={styles.wrapper}>
      <Text style={styles.label}>{label}</Text>
      <TextInput
        style={[styles.input, error && styles.inputError, style]}
        placeholderTextColor="#6b7280"
        {...props}
      />
      {error ? <Text style={styles.error}>{error}</Text> : null}
    </View>
  )
}

const styles = StyleSheet.create({
  wrapper: { marginBottom: 16 },
  label: { color: '#e2e8f0', fontSize: 13, marginBottom: 6, fontWeight: '500' },
  input: {
    backgroundColor: '#1f2937',
    color: '#f9fafb',
    borderWidth: 1,
    borderColor: '#374151',
    borderRadius: 10,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 15,
  },
  inputError: { borderColor: '#f87171' },
  error: { color: '#f87171', fontSize: 12, marginTop: 4 },
})
