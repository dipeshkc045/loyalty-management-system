'use client'

import { useState, useEffect } from 'react'
import { transactionService } from '@/lib/api/transactionService'
import { productService } from '@/lib/api/productService'
import { useMembers } from '@/lib/context/MemberContext'
import { Transaction, Product } from '@/lib/types'
import { Plus, Search, RefreshCw, X, CreditCard, ShoppingBag } from 'lucide-react'

export default function TransactionsPage() {
    const { membersLite: members, loading: membersLoading, refresh: refreshMembers } = useMembers()
    const [transactions, setTransactions] = useState<Transaction[]>([])
    const [products, setProducts] = useState<Product[]>([])
    const [loading, setLoading] = useState(true)
    const [showModal, setShowModal] = useState(false)
    const [submitting, setSubmitting] = useState(false)
    const [success, setSuccess] = useState(false)

    useEffect(() => {
        loadData()
    }, [])

    // Poll for updates if there are pending transactions
    useEffect(() => {
        const hasPending = transactions.some(t => t.status === 'PENDING')
        if (hasPending) {
            const interval = setInterval(() => {
                loadData(true) // Silent reload
            }, 3000)
            return () => clearInterval(interval)
        }
    }, [transactions])

    const loadData = async (silent = false) => {
        try {
            if (!silent) setLoading(true)
            const [txs, prods] = await Promise.all([
                transactionService.getAll(),
                productService.getAll(),
                refreshMembers()
            ])
            setTransactions(txs || [])
            setProducts(prods || [])
        } catch (err) {
            console.error('Failed to load data:', err)
        } finally {
            if (!silent) setLoading(false)
        }
    }

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        setSubmitting(true)
        setSuccess(false)

        const formData = new FormData(e.currentTarget)
        const recipientId = formData.get('recipientId')

        const transactionData: any = {
            memberId: parseInt(formData.get('memberId') as string),
            amount: parseFloat(formData.get('amount') as string),
            paymentMethod: formData.get('paymentMethod') as string,
            productCategory: formData.get('productCategory') as string,
        }

        if (recipientId) {
            transactionData.receiverId = parseInt(recipientId as string)
        }

        try {
            await transactionService.create(transactionData)
            setSuccess(true)
            setShowModal(false)
            loadData() // Reload list
            setTimeout(() => setSuccess(false), 3000)
        } catch (err) {
            console.error('Failed to create transaction:', err)
            alert('Failed to create transaction')
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Transactions</h1>
                    <p className="mt-2 text-gray-600">Monitor and manage customer transactions</p>
                </div>
                <button
                    onClick={() => setShowModal(true)}
                    className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-colors shadow-sm hover:shadow-md font-medium"
                >
                    <Plus size={20} />
                    <span>New Transaction</span>
                </button>
            </div>

            {/* Transaction List */}
            {loading ? (
                <div className="flex justify-center py-20">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-600"></div>
                </div>
            ) : (
                <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50 border-b border-gray-100">
                                <tr>
                                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Date</th>
                                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Sender</th>
                                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Receiver</th>
                                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Amount</th>
                                    <th className="px-6 py-4 text-left text-xs font-bold text-gray-400 uppercase tracking-wider">Payment</th>
                                    <th className="px-6 py-4 text-right text-xs font-bold text-gray-400 uppercase tracking-wider">Points</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-100">
                                {transactions.length === 0 ? (
                                    <tr>
                                        <td colSpan={6} className="px-6 py-12 text-center text-gray-500 italic">
                                            No transactions found
                                        </td>
                                    </tr>
                                ) : (
                                    transactions.map((tx) => {
                                        const sender = members.find(m => m.id === tx.memberId)
                                        // Use 'any' cast to access receiverId if it's not in the type definition yet
                                        const receiverId = (tx as any).receiverId
                                        const receiver = receiverId ? members.find(m => m.id === receiverId) : null

                                        return (
                                            <tr key={tx.id} className="hover:bg-gray-50/50 transition-colors">
                                                <td className="px-6 py-4 text-sm text-gray-500 font-medium">
                                                    {new Date(tx.transactionDate || '').toLocaleDateString()}
                                                    <span className="text-[10px] text-gray-400 block mt-0.5">
                                                        {new Date(tx.transactionDate || '').toLocaleTimeString()}
                                                    </span>
                                                </td>
                                                <td className="px-6 py-4">
                                                    <div className="flex items-center gap-3">
                                                        <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold text-xs uppercase">
                                                            {sender?.name.substring(0, 2) || '??'}
                                                        </div>
                                                        <div>
                                                            <div className="text-sm font-bold text-gray-900">{sender?.name || `ID: ${tx.memberId}`}</div>
                                                            <div className="text-xs text-gray-500 font-medium">{sender?.email}</div>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td className="px-6 py-4">
                                                    {receiver ? (
                                                        <div className="flex items-center gap-3">
                                                            <div className="w-8 h-8 rounded-full bg-purple-100 flex items-center justify-center text-purple-600 font-bold text-xs uppercase">
                                                                {receiver.name.substring(0, 2)}
                                                            </div>
                                                            <div>
                                                                <div className="text-sm font-bold text-gray-900">{receiver.name}</div>
                                                                <div className="text-xs text-gray-500 font-medium">{receiver.email}</div>
                                                            </div>
                                                        </div>
                                                    ) : (
                                                        <span className="text-xs text-gray-400 italic">--</span>
                                                    )}
                                                </td>
                                                <td className="px-6 py-4">
                                                    <span className="text-sm font-bold text-gray-900">${tx.amount.toFixed(2)}</span>
                                                </td>
                                                <td className="px-6 py-4">
                                                    <div className="flex flex-col">
                                                        <span className="text-xs font-bold uppercase tracking-wider text-gray-700">{tx.paymentMethod}</span>
                                                    </div>
                                                </td>
                                                <td className="px-6 py-4 text-right">
                                                    {tx.status === 'PENDING' ? (
                                                        <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-bold bg-yellow-100 text-yellow-800">
                                                            <RefreshCw size={10} className="animate-spin" />
                                                            Processing
                                                        </span>
                                                    ) : (
                                                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-green-100 text-green-800">
                                                            +{tx.pointsEarned} pts
                                                        </span>
                                                    )}
                                                </td>
                                            </tr>
                                        )
                                    })
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* Create Transaction Modal */}
            {showModal && (
                <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                        <div className="sticky top-0 bg-white px-6 py-4 border-b border-gray-100 flex justify-between items-center">
                            <div>
                                <h2 className="text-xl font-bold text-gray-900">New Transaction</h2>
                                <p className="text-xs text-gray-500 mt-1">Record a transaction to award points</p>
                            </div>
                            <button
                                onClick={() => setShowModal(false)}
                                className="p-2 hover:bg-gray-100 rounded-full transition-colors text-gray-400 hover:text-gray-600"
                            >
                                <X size={20} />
                            </button>
                        </div>

                        <form onSubmit={handleSubmit} className="p-6 space-y-6">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div>
                                    <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Sender *</label>
                                    <select
                                        name="memberId"
                                        required
                                        className="w-full px-4 py-3 bg-gray-50 border-transparent rounded-xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-sm font-medium text-gray-900"
                                    >
                                        <option value="">Select Sender</option>
                                        {members.map(m => (
                                            <option key={m.id} value={m.id}>{m.name} ({m.email})</option>
                                        ))}
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Receiver (Optional)</label>
                                    <select
                                        name="recipientId"
                                        className="w-full px-4 py-3 bg-gray-50 border-transparent rounded-xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-sm font-medium text-gray-900"
                                    >
                                        <option value="">Select Receiver</option>
                                        {members.map(m => (
                                            <option key={m.id} value={m.id}>{m.name} ({m.email})</option>
                                        ))}
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Amount *</label>
                                    <div className="relative">
                                        <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 font-bold">$</span>
                                        <input
                                            type="number"
                                            name="amount"
                                            required
                                            min="0.01"
                                            step="0.01"
                                            placeholder="0.00"
                                            className="w-full pl-8 pr-4 py-3 bg-gray-50 border-transparent rounded-xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-sm font-medium text-gray-900"
                                        />
                                    </div>
                                </div>

                                <div>
                                    <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Payment Method *</label>
                                    <select
                                        name="paymentMethod"
                                        required
                                        className="w-full px-4 py-3 bg-gray-50 border-transparent rounded-xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-sm font-medium text-gray-900"
                                    >
                                        <option value="">Select Method</option>
                                        {products.length > 0 ? (
                                            products.map(p => (
                                                <option key={p.id} value={p.name}>{p.name}</option>
                                            ))
                                        ) : (
                                            <>
                                                <option value="CASH">Cash</option>
                                                <option value="CREDIT_CARD">Credit Card</option>
                                                <option value="DEBIT_CARD">Debit Card</option>
                                                <option value="QR">QR Code</option>
                                            </>
                                        )}
                                    </select>
                                    <p className="mt-1 text-[10px] text-gray-400 px-1 italic">
                                        * Populated from Products
                                    </p>
                                </div>

                                <div className="col-span-full">
                                    <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Product Category (Optional)</label>
                                    <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                                        {['ELECTRONICS', 'FOOD', 'CLOTHING', 'GROCERIES', 'SERVICES', 'OTHER'].map(cat => (
                                            <label key={cat} className="cursor-pointer">
                                                <input type="radio" name="productCategory" value={cat} className="peer sr-only" />
                                                <div className="px-3 py-2 rounded-xl bg-gray-50 border border-transparent peer-checked:bg-blue-50 peer-checked:border-blue-500 peer-checked:text-blue-700 text-xs font-bold text-gray-600 text-center transition-all hover:bg-gray-100">
                                                    {cat}
                                                </div>
                                            </label>
                                        ))}
                                    </div>
                                </div>
                            </div>

                            <div className="pt-4 border-t border-gray-100 flex gap-3 justify-end">
                                <button
                                    type="button"
                                    onClick={() => setShowModal(false)}
                                    className="px-6 py-2.5 rounded-xl text-sm font-bold text-gray-500 hover:bg-gray-100 transition-colors"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={submitting}
                                    className="px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-sm font-bold shadow-lg shadow-blue-500/30 transition-all hover:shadow-blue-500/50 disabled:opacity-50 disabled:cursor-not-allowed"
                                >
                                    {submitting ? 'Processing...' : 'Create Transaction'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    )
}
