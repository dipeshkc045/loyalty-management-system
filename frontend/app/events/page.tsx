'use client'

import { useState } from 'react'
import { eventService } from '@/lib/api/eventService'
import { useMembers } from '@/lib/context/MemberContext'

export default function EventsPage() {
    const { membersLite: members, loading: membersLoading } = useMembers()
    const [loading, setLoading] = useState(false)
    const [success, setSuccess] = useState<string | null>(null)

    const handleOnboarding = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        setLoading(true)
        setSuccess(null)

        const formData = new FormData(e.currentTarget)
        const memberId = parseInt(formData.get('memberId') as string)

        try {
            await eventService.onboard(memberId)
            setSuccess('Onboarding bonus awarded successfully!')
                ; (e.target as HTMLFormElement).reset()
        } catch (err) {
            console.error('Failed to trigger onboarding:', err)
        } finally {
            setLoading(false)
        }
    }

    const handleReferral = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        setLoading(true)
        setSuccess(null)

        const formData = new FormData(e.currentTarget)
        const referrerId = parseInt(formData.get('referrerId') as string)
        const refereeId = parseInt(formData.get('refereeId') as string)

        try {
            await eventService.referral(referrerId, refereeId)
            setSuccess('Referral bonuses awarded successfully!')
                ; (e.target as HTMLFormElement).reset()
        } catch (err) {
            console.error('Failed to trigger referral:', err)
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="max-w-4xl mx-auto space-y-6">
            <div>
                <h1 className="text-3xl font-bold text-gray-900">Events</h1>
                <p className="mt-2 text-gray-600">Trigger loyalty events and bonuses</p>
            </div>

            {success && (
                <div className="bg green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
                    {success}
                </div>
            )}

            <div className="grid md:grid-cols-2 gap-6">
                {/* Onboarding Event */}
                <div className="bg-white rounded-xl shadow-md p-6 border border-gray-100">
                    <h2 className="text-xl font-semibold text-gray-900 mb-4">Onboarding Bonus</h2>
                    <p className="text-sm text-gray-600 mb-4">Award 500 welcome bonus points to a new member</p>

                    <form onSubmit={handleOnboarding} className="space-y-4">
                        <div>
                            <label htmlFor="onboard-member" className="block text-sm font-medium text-gray-700 mb-2">
                                Select Member
                            </label>
                            <select
                                id="onboard-member"
                                name="memberId"
                                required
                                disabled={membersLoading}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50"
                            >
                                <option value="">{membersLoading ? 'Loading...' : 'Choose a member'}</option>
                                {members.map((member) => (
                                    <option key={member.id} value={member.id}>
                                        {member.name} ({member.email})
                                    </option>
                                ))}
                            </select>
                        </div>

                        <button
                            type="submit"
                            disabled={loading || membersLoading}
                            className="w-full bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium transition-colors disabled:opacity-50 shadow-md hover:shadow-lg"
                        >
                            {loading ? 'Processing...' : 'Award Onboarding Bonus'}
                        </button>
                    </form>
                </div>

                {/* Referral Event */}
                <div className="bg-white rounded-xl shadow-md p-6 border border-gray-100">
                    <h2 className="text-xl font-semibold text-gray-900 mb-4">Referral Bonus</h2>
                    <p className="text-sm text-gray-600 mb-4">Award referral bonuses (1000 to referrer, 500 to referee)</p>

                    <form onSubmit={handleReferral} className="space-y-4">
                        <div>
                            <label htmlFor="referrer" className="block text-sm font-medium text-gray-700 mb-2">
                                Referrer (Existing Member)
                            </label>
                            <select
                                id="referrer"
                                name="referrerId"
                                required
                                disabled={membersLoading}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50"
                            >
                                <option value="">{membersLoading ? 'Loading...' : 'Choose referrer'}</option>
                                {members.map((member) => (
                                    <option key={member.id} value={member.id}>
                                        {member.name} ({member.email})
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label htmlFor="referee" className="block text-sm font-medium text-gray-700 mb-2">
                                Referee (New Member)
                            </label>
                            <select
                                id="referee"
                                name="refereeId"
                                required
                                disabled={membersLoading}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50"
                            >
                                <option value="">{membersLoading ? 'Loading...' : 'Choose referee'}</option>
                                {members.map((member) => (
                                    <option key={member.id} value={member.id}>
                                        {member.name} ({member.email})
                                    </option>
                                ))}
                            </select>
                        </div>

                        <button
                            type="submit"
                            disabled={loading || membersLoading}
                            className="w-full bg-purple-600 hover:bg-purple-700 text-white px-6 py-3 rounded-lg font-medium transition-colors disabled:opacity-50 shadow-md hover:shadow-lg"
                        >
                            {loading ? 'Processing...' : 'Award Referral Bonuses'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    )
}
