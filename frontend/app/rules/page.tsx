'use client'

import { useState, useEffect } from 'react'
import { ruleService } from '@/lib/api/ruleService'
import { productService } from '@/lib/api/productService'
import { Rule, Product } from '@/lib/types'
import { Trash2, Edit2, Plus, RefreshCw, Layers, Calendar, ShoppingBag, Award } from 'lucide-react'

export default function RulesPage() {
    const [rules, setRules] = useState<Rule[]>([])
    const [loading, setLoading] = useState(true)
    const [activeTab, setActiveTab] = useState<'PRODUCT' | 'REWARD' | 'TRANSACTION' | 'EVENT'>('EVENT')
    const [showForm, setShowForm] = useState(false)
    const [editingRule, setEditingRule] = useState<Rule | null>(null)
    const [products, setProducts] = useState<Product[]>([])
    const [rewardRules, setRewardRules] = useState<Rule[]>([])

    // Form states
    const [formData, setFormData] = useState<Partial<Rule>>({
        ruleName: '',
        ruleType: 'EVENT',
        priority: 1,
        isActive: true,
        conditions: '{}',
        actions: '{}'
    })

    useEffect(() => {
        loadRules()
        loadProducts()
        loadRewardRules()
    }, [])

    const loadProducts = async () => {
        try {
            const data = await productService.getAll()
            setProducts(data)
        } catch (err) {
            console.error('Failed to load products:', err)
        }
    }

    const loadRules = async () => {
        try {
            setLoading(true)
            const data = await ruleService.getAll()
            setRules(data)
        } catch (err) {
            console.error('Failed to load rules:', err)
        } finally {
            setLoading(false)
        }
    }

    const loadRewardRules = async () => {
        try {
            const data = await ruleService.getAll()
            const rewards = data.filter(r => r.ruleType === 'REWARD')
            setRewardRules(rewards)
        } catch (err) {
            console.error('Failed to load reward rules:', err)
        }
    }

    const handleCreate = () => {
        setEditingRule(null)
        setFormData({
            ruleName: '',
            ruleType: activeTab,
            priority: 1,
            isActive: true,
            conditions: '{}',
            actions: '[]',
            evaluationType: 'TRANSACTION',
            rewardType: 'POINTS'
        })
        setShowForm(true)
    }

    const formatJson = (data: any): string => {
        if (typeof data === 'string') return data;
        try {
            return JSON.stringify(data);
        } catch (e) {
            return String(data);
        }
    }

    const handleEdit = (rule: Rule) => {
        setEditingRule(rule)
        setFormData({
            ...rule,
            conditions: formatJson(rule.conditions),
            actions: formatJson(rule.actions)
        })
        setShowForm(true)
    }

    const handleDelete = async (id: number) => {
        if (!confirm('Are you sure you want to delete this rule?')) return
        try {
            await ruleService.delete(id)
            loadRules()
        } catch (err) {
            console.error('Failed to delete rule:', err)
        }
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        try {
            // Try to parse JSON fields to send as objects if possible, mimicking backend expectation if any
            let conditionsToSave = formData.conditions;
            let actionsToSave = formData.actions;

            try {
                if (typeof formData.conditions === 'string') {
                    conditionsToSave = JSON.parse(formData.conditions);
                }
            } catch (e) {
                // Keep as string if not valid JSON
            }

            try {
                if (typeof formData.actions === 'string') {
                    actionsToSave = JSON.parse(formData.actions);
                }
            } catch (e) {
                // Keep as string
            }

            const payload = { ...formData, conditions: conditionsToSave, actions: actionsToSave };

            if (editingRule?.id) {
                await ruleService.update(editingRule.id, payload)
            } else {
                await ruleService.create(payload)
            }
            setShowForm(false)
            loadRules()
        } catch (err) {
            console.error('Failed to save rule:', err)
            alert('Failed to save rule. Please check your JSON format.');
        }
    }

    const filteredRules = rules.filter(r => r.ruleType === activeTab || (!r.ruleType && activeTab === 'EVENT')) // Default to EVENT if type missing for now

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Rule Management</h1>
                    <p className="mt-2 text-gray-600">Configure loyalty logic for events, transactions, and products</p>
                </div>
                <button
                    onClick={handleCreate}
                    className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium transition-colors shadow-md flex items-center gap-2"
                >
                    <Plus size={20} />
                    Create Rule
                </button>
            </div>

            {/* Tabs */}
            <div className="border-b border-gray-200">
                <nav className="-mb-px flex space-x-8">
                    {['EVENT', 'TRANSACTION', 'PRODUCT', 'REWARD'].map((tab) => (
                        <button
                            key={tab}
                            onClick={() => setActiveTab(tab as any)}
                            className={`${activeTab === tab
                                ? 'border-blue-500 text-blue-600'
                                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                } whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm capitalize`}
                        >
                            {tab.toLowerCase()} Rules
                        </button>
                    ))}
                </nav>
            </div>

            {/* Rule List - Card Based */}
            {loading ? (
                <div className="flex justify-center items-center py-20">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-600"></div>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {filteredRules.map((rule) => (
                        <div
                            key={rule.id}
                            className="bg-white rounded-2xl shadow-sm border border-gray-100 hover:shadow-xl hover:translate-y-[-4px] transition-all duration-300 group overflow-hidden"
                        >
                            <div className="p-1 bg-gradient-to-r from-blue-500 to-indigo-600 opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                            <div className="p-6">
                                <div className="flex justify-between items-start mb-4">
                                    <div className="flex items-center gap-3">
                                        <div className="p-2 bg-blue-50 rounded-xl text-blue-600">
                                            {activeTab === 'PRODUCT' ? <ShoppingBag size={20} /> :
                                                activeTab === 'EVENT' ? <Layers size={20} /> :
                                                    activeTab === 'TRANSACTION' ? <RefreshCw size={20} /> :
                                                        <Award size={20} />}
                                        </div>
                                        <div>
                                            <h3 className="font-bold text-gray-900 group-hover:text-blue-600 transition-colors uppercase tracking-tight">{rule.ruleName}</h3>
                                            <p className="text-xs text-gray-500">Priority: {rule.priority || 1}</p>
                                        </div>
                                    </div>
                                    <div className="flex gap-2">
                                        <span className={`px-3 py-1 text-[10px] font-bold uppercase tracking-wider rounded-full ${rule.isActive ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                                            {rule.isActive ? 'Active' : 'Inactive'}
                                        </span>
                                        {rule.targetTier && (
                                            <span className="px-3 py-1 text-[10px] font-bold uppercase tracking-wider rounded-full bg-blue-600 text-white shadow-sm">
                                                {rule.targetTier}
                                            </span>
                                        )}
                                    </div>
                                </div>

                                <div className="space-y-4">
                                    {activeTab === 'TRANSACTION' && (
                                        <div className="flex items-center gap-2 text-sm text-gray-600">
                                            <Calendar size={14} className="text-gray-400" />
                                            <span>{rule.evaluationType || 'Per Transaction'}</span>
                                        </div>
                                    )}

                                    {activeTab === 'TRANSACTION' ? (
                                        <div className="space-y-1.5">
                                            {(() => {
                                                const actions = Array.isArray(rule.actions) ? rule.actions : [];
                                                const tieredAction = actions.find((a: any) => a.type === 'TIERED_POINTS');
                                                if (!tieredAction || !tieredAction.ranges) return <span className="text-xs text-gray-400 italic">No ranges defined</span>;

                                                return tieredAction.ranges.slice(0, 3).map((r: any, i: number) => (
                                                    <div key={i} className="flex justify-between items-center bg-gray-50 px-2 py-1.5 rounded-lg border border-gray-100/50">
                                                        <span className="text-[10px] font-bold text-gray-500">${r.min} - ${r.max || '∞'}</span>
                                                        <div className="text-right">
                                                            <span className="text-xs font-black text-blue-600">+{r.points} pts</span>
                                                            {r.multiplier > 0 && (
                                                                <span className="block text-[9px] text-indigo-500 font-bold">+{r.multiplier}x</span>
                                                            )}
                                                        </div>
                                                    </div>
                                                ));
                                            })()}
                                            {(() => {
                                                const actions = Array.isArray(rule.actions) ? rule.actions : [];
                                                const tieredAction = actions.find((a: any) => a.type === 'TIERED_POINTS');
                                                return tieredAction?.ranges?.length > 3 && (
                                                    <p className="text-[9px] text-center text-gray-400 font-bold uppercase tracking-wider">+{tieredAction.ranges.length - 3} more tiers</p>
                                                );
                                            })()}
                                        </div>
                                    ) : rule.targetProductCodes && rule.targetProductCodes.length > 0 ? (
                                        <div className="flex flex-wrap gap-1.5">
                                            {rule.targetProductCodes.map(code => (
                                                <span key={code} className="inline-flex items-center px-2 py-0.5 rounded-lg text-[10px] font-semibold bg-gray-50 text-gray-700 border border-gray-100 group-hover:border-blue-200 group-hover:bg-blue-50 transition-colors">
                                                    {code}
                                                </span>
                                            ))}
                                        </div>
                                    ) : null}

                                    <div className="pt-4 border-t border-gray-50 flex items-center justify-between">
                                        <div className="flex items-center gap-1.5 text-blue-600 font-bold text-sm">
                                            <Award size={16} />
                                            {activeTab === 'TRANSACTION' ? 'Tiered Points' : (rule.rewardType || 'Points')}
                                        </div>
                                        <div className="flex gap-2">
                                            <button
                                                onClick={() => handleEdit(rule)}
                                                className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                                title="Edit Rule"
                                            >
                                                <Edit2 size={18} />
                                            </button>
                                            <button
                                                onClick={() => handleDelete(rule.id!)}
                                                className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                                title="Delete Rule"
                                            >
                                                <Trash2 size={18} />
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                    {filteredRules.length === 0 && (
                        <div className="col-span-full py-20 text-center bg-gray-50 rounded-3xl border-2 border-dashed border-gray-200">
                            <Layers size={48} className="mx-auto text-gray-300 mb-4" />
                            <h3 className="text-lg font-medium text-gray-900">No {activeTab.toLowerCase()} rules found</h3>
                            <p className="text-gray-500 mt-1">Create your first rule to get started</p>
                        </div>
                    )}
                </div>
            )}

            {showForm && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 overflow-y-auto">
                    {/* Glassmorphism Backdrop */}
                    <div
                        className="fixed inset-0 bg-gray-900/40 backdrop-blur-md transition-opacity"
                        onClick={() => setShowForm(false)}
                    />

                    {/* Modal Content */}
                    <div className="relative bg-white rounded-3xl shadow-2xl w-full max-w-2xl transform transition-all animate-in zoom-in-95 duration-300 overflow-hidden border border-white/20">
                        {/* Gradient Header */}
                        <div className="bg-gradient-to-r from-blue-600 to-indigo-700 px-8 py-6 text-white flex justify-between items-center">
                            <div className="flex items-center gap-4">
                                <div className="p-3 bg-white/10 rounded-2xl backdrop-blur-sm">
                                    {activeTab === 'PRODUCT' ? <ShoppingBag size={24} /> :
                                        activeTab === 'EVENT' ? <Layers size={24} /> :
                                            activeTab === 'TRANSACTION' ? <RefreshCw size={24} /> :
                                                <Award size={24} />}
                                </div>
                                <h2 className="text-2xl font-bold tracking-tight">
                                    {editingRule ? 'Edit Rule' : 'Create New Rule'}
                                </h2>
                            </div>
                            <button
                                onClick={() => setShowForm(false)}
                                className="p-2 hover:bg-white/10 rounded-xl transition-colors"
                            >
                                <Plus size={24} className="rotate-45" />
                            </button>
                        </div>

                        <form onSubmit={handleSubmit} className="p-8 space-y-8">
                            <div>
                                <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Rule Name</label>
                                <input
                                    type="text"
                                    required
                                    placeholder="Enter a descriptive rule name..."
                                    value={formData.ruleName}
                                    onChange={(e) => setFormData({ ...formData, ruleName: e.target.value })}
                                    className="w-full px-5 py-4 bg-gray-50 border-transparent rounded-2xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-gray-900 placeholder-gray-400 font-medium"
                                />
                            </div>

                            {/* Status field - always visible */}
                            <div>
                                <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Status</label>
                                <select
                                    value={formData.isActive ? 'true' : 'false'}
                                    onChange={(e) => setFormData({ ...formData, isActive: e.target.value === 'true' })}
                                    className="w-full px-5 py-4 bg-gray-50 border-transparent rounded-2xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-gray-900 font-medium appearance-none"
                                >
                                    <option value="true">Active & Visible</option>
                                    <option value="false">Hidden / Draft</option>
                                </select>
                            </div>

                            {/* Fields only for TRANSACTION rules */}
                            {activeTab === 'TRANSACTION' && (
                                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                                    <div>
                                        <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Priority</label>
                                        <input
                                            type="number"
                                            required
                                            value={formData.priority || 1}
                                            onChange={(e) => setFormData({ ...formData, priority: Number(e.target.value) })}
                                            className="w-full px-5 py-4 bg-gray-50 border-transparent rounded-2xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-gray-900 font-medium"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Target Member Tier</label>
                                        <select
                                            value={formData.targetTier || ''}
                                            onChange={(e) => setFormData({ ...formData, targetTier: e.target.value || undefined })}
                                            className="w-full px-5 py-4 bg-gray-50 border-transparent rounded-2xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-gray-900 font-medium appearance-none"
                                        >
                                            <option value="">All Tiers (No Filter)</option>
                                            <option value="BRONZE">Bronze</option>
                                            <option value="SILVER">Silver</option>
                                            <option value="GOLD">Gold</option>
                                            <option value="PLATINUM">Platinum</option>
                                        </select>
                                    </div>
                                    <div>
                                        <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Evaluation Period</label>
                                        <select
                                            value={formData.evaluationType || 'TRANSACTION'}
                                            onChange={(e) => setFormData({ ...formData, evaluationType: e.target.value as any })}
                                            className="w-full px-5 py-4 bg-gray-50 border-transparent rounded-2xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-gray-900 font-medium appearance-none"
                                        >
                                            <option value="TRANSACTION">Per Transaction</option>
                                            <option value="MONTHLY">Monthly Aggregate</option>
                                            <option value="QUARTERLY">Quarterly Aggregate</option>
                                        </select>
                                    </div>
                                </div>
                            )}

                            {/* Point Reward and Reward Type - visible for EVENT rules */}
                            {activeTab === 'EVENT' && (
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                                    <div>
                                        <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Reward Type</label>
                                        <select
                                            value={formData.rewardType || ''}
                                            onChange={(e) => setFormData({ ...formData, rewardType: e.target.value as any })}
                                            className="w-full px-5 py-4 bg-gray-50 border-transparent rounded-2xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-gray-900 font-medium appearance-none"
                                        >
                                            <option value="">Select Reward Type</option>
                                            {rewardRules.map(rule => (
                                                <option key={rule.id} value={rule.ruleName}>{rule.ruleName}</option>
                                            ))}
                                            {rewardRules.length === 0 && (
                                                <>
                                                    <option value="POINTS">Points Accumulation</option>
                                                    <option value="DISCOUNT">Direct Discount %</option>
                                                </>
                                            )}
                                        </select>
                                    </div>
                                    <div>
                                        <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Point Reward</label>
                                        <input
                                            type="number"
                                            placeholder="Enter reward points..."
                                            required
                                            value={(() => {
                                                try {
                                                    const actions = JSON.parse(formData.actions as string || '{}');
                                                    if (Array.isArray(actions)) {
                                                        const awardAction = actions.find((a: any) => a.points !== undefined);
                                                        return awardAction ? awardAction.points : 0;
                                                    }
                                                    return actions.points || 0;
                                                } catch (e) { return 0; }
                                            })()}
                                            onChange={(e) => {
                                                try {
                                                    const actions = JSON.parse(formData.actions as string || '{}');
                                                    const val = Number(e.target.value);
                                                    if (Array.isArray(actions)) {
                                                        let awardAction = actions.find((a: any) => a.type === 'AWARD_POINTS');
                                                        if (!awardAction) {
                                                            awardAction = { type: 'AWARD_POINTS', points: val };
                                                            actions.push(awardAction);
                                                        } else {
                                                            awardAction.points = val;
                                                        }
                                                    } else {
                                                        actions.points = val;
                                                    }
                                                    setFormData({ ...formData, actions: JSON.stringify(actions) });
                                                } catch (e) { }
                                            }}
                                            className="w-full px-5 py-4 bg-gray-50 border-transparent rounded-2xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-gray-900 font-medium"
                                        />
                                    </div>
                                </div>
                            )}

                            {/* Expiry Date - visible for EVENT rules */}
                            {activeTab === 'EVENT' && (
                                <div>
                                    <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Expiry Date</label>
                                    <input
                                        type="date"
                                        value={formData.validUntil ? formData.validUntil.split('T')[0] : ''}
                                        onChange={(e) => setFormData({ ...formData, validUntil: e.target.value ? e.target.value + 'T23:59:59' : undefined })}
                                        className="w-full px-5 py-4 bg-gray-50 border-transparent rounded-2xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-gray-900 font-medium"
                                    />
                                </div>
                            )}

                            {activeTab === 'TRANSACTION' ? (
                                <div className="space-y-6">
                                    <div className="flex justify-between items-center px-1">
                                        <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest">Transaction Ranges & Rewards</label>
                                        <button
                                            type="button"
                                            onClick={() => {
                                                const currentActions = JSON.parse(formData.actions as string || '[]');
                                                let tieredAction = currentActions.find((a: any) => a.type === 'TIERED_POINTS');
                                                if (!tieredAction) {
                                                    tieredAction = { type: 'TIERED_POINTS', ranges: [] };
                                                    currentActions.push(tieredAction);
                                                }
                                                tieredAction.ranges.push({ min: 0, max: 0, points: 0, multiplier: 0 });
                                                setFormData({ ...formData, actions: JSON.stringify(currentActions) });
                                            }}
                                            className="px-3 py-1.5 bg-blue-50 text-blue-600 rounded-lg text-[10px] font-bold uppercase tracking-wider hover:bg-blue-100 transition-colors flex items-center gap-1"
                                        >
                                            <Plus size={12} /> Add Range
                                        </button>
                                    </div>

                                    <div className="space-y-3">
                                        {(() => {
                                            const actions = JSON.parse(formData.actions as string || '[]');
                                            const tieredAction = actions.find((a: any) => a.type === 'TIERED_POINTS') || { ranges: [] };

                                            if (tieredAction.ranges.length === 0) {
                                                return (
                                                    <div className="py-8 text-center bg-gray-50 rounded-2xl text-gray-400 italic text-sm border-2 border-dashed border-gray-100">
                                                        No ranges defined. Add a range to award points.
                                                    </div>
                                                );
                                            }

                                            return tieredAction.ranges.map((range: any, idx: number) => (
                                                <div key={idx} className="flex gap-3 items-center bg-white p-3 rounded-2xl border border-gray-100 shadow-sm animate-in slide-in-from-left-2 duration-300">
                                                    <div className="flex-1">
                                                        <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1 ml-1">Min ($)</label>
                                                        <input
                                                            type="number"
                                                            value={range.min}
                                                            onChange={(e) => {
                                                                range.min = Number(e.target.value);
                                                                setFormData({ ...formData, actions: JSON.stringify(actions) });
                                                            }}
                                                            className="w-full px-3 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-500/20 text-sm font-medium text-gray-900"
                                                        />
                                                    </div>
                                                    <div className="flex-1">
                                                        <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1 ml-1">Max ($)</label>
                                                        <input
                                                            type="number"
                                                            value={range.max}
                                                            onChange={(e) => {
                                                                range.max = Number(e.target.value);
                                                                setFormData({ ...formData, actions: JSON.stringify(actions) });
                                                            }}
                                                            className="w-full px-3 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-500/20 text-sm font-medium text-gray-900"
                                                        />
                                                    </div>
                                                    <div className="flex-1">
                                                        <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1 ml-1">Points</label>
                                                        <input
                                                            type="number"
                                                            value={range.points}
                                                            onChange={(e) => {
                                                                range.points = Number(e.target.value);
                                                                setFormData({ ...formData, actions: JSON.stringify(actions) });
                                                            }}
                                                            className="w-full px-3 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-500/20 text-sm font-medium text-gray-900"
                                                        />
                                                    </div>
                                                    <div className="flex-1">
                                                        <label className="block text-[10px] font-bold text-gray-400 uppercase mb-1 ml-1">Multiplier</label>
                                                        <input
                                                            type="number"
                                                            step="0.01"
                                                            value={range.multiplier || 0}
                                                            onChange={(e) => {
                                                                range.multiplier = Number(e.target.value);
                                                                setFormData({ ...formData, actions: JSON.stringify(actions) });
                                                            }}
                                                            className="w-full px-3 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-500/20 text-sm font-medium text-gray-900"
                                                        />
                                                    </div>
                                                    <button
                                                        type="button"
                                                        onClick={() => {
                                                            tieredAction.ranges.splice(idx, 1);
                                                            setFormData({ ...formData, actions: JSON.stringify(actions) });
                                                        }}
                                                        className="mt-5 p-2 text-gray-300 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                                                    >
                                                        <Trash2 size={16} />
                                                    </button>
                                                </div>
                                            ));
                                        })()}
                                    </div>
                                </div>
                            ) : null}

                            {/* Target Products - only for PRODUCT rules */}
                            {activeTab === 'PRODUCT' && (
                                <div className="space-y-4">
                                    <div className="flex justify-between items-center px-1">
                                        <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest">Target Product(s)</label>
                                        <button
                                            type="button"
                                            onClick={() => {
                                                const allCodes = products.map(p => p.code);
                                                const currentCodes = formData.targetProductCodes || [];
                                                setFormData({
                                                    ...formData,
                                                    targetProductCodes: currentCodes.length === allCodes.length ? [] : allCodes
                                                });
                                            }}
                                            className="text-[10px] font-bold uppercase tracking-wider text-blue-600 hover:text-blue-800"
                                        >
                                            {(formData.targetProductCodes || []).length === products.length ? 'Deselect All' : 'Select All Available'}
                                        </button>
                                    </div>
                                    <div className="grid grid-cols-2 gap-3 max-h-48 overflow-y-auto p-1 scrollbar-thin scrollbar-thumb-gray-200">
                                        {products.length === 0 ? (
                                            <div className="col-span-full py-8 text-center bg-gray-50 rounded-2xl text-gray-400 italic text-sm">
                                                No products available to target
                                            </div>
                                        ) : (
                                            products.map(p => {
                                                const isSelected = (formData.targetProductCodes || []).includes(p.code);
                                                return (
                                                    <label
                                                        key={p.id}
                                                        className={`group relative flex items-center gap-3 p-4 rounded-2xl cursor-pointer transition-all border-2 ${isSelected
                                                            ? 'bg-blue-50 border-blue-500 shadow-sm'
                                                            : 'bg-white border-gray-100 hover:border-blue-200 hover:bg-blue-50/30'
                                                            }`}
                                                    >
                                                        <input
                                                            type="checkbox"
                                                            className="w-5 h-5 rounded-lg text-blue-600 focus:ring-offset-0 focus:ring-blue-500 border-gray-300"
                                                            checked={isSelected}
                                                            onChange={() => {
                                                                const current = formData.targetProductCodes || [];
                                                                setFormData({
                                                                    ...formData,
                                                                    targetProductCodes: isSelected ? current.filter(c => c !== p.code) : [...current, p.code]
                                                                });
                                                            }}
                                                        />
                                                        <div className="flex flex-col min-w-0">
                                                            <span className="text-sm font-bold text-gray-900 truncate tracking-tight">{p.name}</span>
                                                            <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">{p.code}</span>
                                                        </div>
                                                    </label>
                                                );
                                            })
                                        )}
                                    </div>
                                </div>
                            )}

                            {/* Reward Type and Expiry Date - only for TRANSACTION and PRODUCT rules */}
                            {(activeTab === 'TRANSACTION' || activeTab === 'PRODUCT') && (
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                                    <div>
                                        <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Reward Type</label>
                                        <select
                                            value={formData.rewardType || ''}
                                            onChange={(e) => setFormData({ ...formData, rewardType: e.target.value as any })}
                                            className="w-full px-5 py-4 bg-gray-50 border-transparent rounded-2xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-gray-900 font-medium appearance-none"
                                        >
                                            <option value="">Select Reward Type</option>
                                            {rewardRules.map(rule => (
                                                <option key={rule.id} value={rule.ruleName}>{rule.ruleName}</option>
                                            ))}
                                            {rewardRules.length === 0 && (
                                                <>
                                                    <option value="POINTS">Points Accumulation</option>
                                                    <option value="DISCOUNT">Direct Discount %</option>
                                                </>
                                            )}
                                        </select>
                                    </div>
                                    <div>
                                        <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2 px-1">Expiry Date</label>
                                        <input
                                            type="date"
                                            value={formData.validUntil ? formData.validUntil.split('T')[0] : ''}
                                            onChange={(e) => setFormData({ ...formData, validUntil: e.target.value ? e.target.value + 'T23:59:59' : undefined })}
                                            className="w-full px-5 py-4 bg-gray-50 border-transparent rounded-2xl focus:bg-white focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all text-gray-900 font-medium"
                                        />
                                    </div>
                                </div>
                            )}

                            <div className="flex gap-4 pt-4 sticky bottom-0 bg-white/80 backdrop-blur-sm -mx-8 px-8 py-4 border-t border-gray-100">
                                <button
                                    type="button"
                                    onClick={() => setShowForm(false)}
                                    className="flex-1 px-8 py-4 text-gray-500 hover:text-gray-900 font-bold uppercase tracking-widest text-xs transition-colors"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="flex-[2] px-8 py-4 bg-gradient-to-r from-blue-600 to-indigo-700 text-white rounded-2xl font-bold uppercase tracking-widest text-xs shadow-xl shadow-blue-500/20 hover:scale-[1.02] active:scale-95 transition-all"
                                >
                                    {editingRule ? 'Save Changes' : 'Create Rule'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    )
}
