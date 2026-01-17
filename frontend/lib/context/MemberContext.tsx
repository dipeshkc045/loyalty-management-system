'use client'

import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { memberService } from '@/lib/api/memberService'
import { Member } from '@/lib/types'

interface MemberContextType {
    membersLite: Member[]
    loading: boolean
    error: string | null
    refresh: () => Promise<void>
}

const MemberContext = createContext<MemberContextType | undefined>(undefined)

export function MemberProvider({ children }: { children: ReactNode }) {
    const [membersLite, setMembersLite] = useState<Member[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const loadMembers = async () => {
        try {
            setLoading(true)
            const data = await memberService.getLite(100)
            setMembersLite(data)
            setError(null)
        } catch (err: any) {
            setError(err.message)
            console.error('Failed to load members for context:', err)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadMembers()
    }, [])

    return (
        <MemberContext.Provider value={{ membersLite, loading, error, refresh: loadMembers }}>
            {children}
        </MemberContext.Provider>
    )
}

export function useMembers() {
    const context = useContext(MemberContext)
    if (!context) {
        throw new Error('useMembers must be used within MemberProvider')
    }
    return context
}
