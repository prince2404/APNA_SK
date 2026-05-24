import React, { useState, useEffect } from 'react';
import { CreditCard, Plus, Trash2, ShieldAlert, CheckCircle2, User, Search, Eye } from 'lucide-react';
import { healthCardApi } from '@/api/healthCardApi';
import { useAuthStore } from '@/store/useAuthStore';
import { PageHeader } from '@/components/common/PageHeader';

export default function HealthCardsPage() {
  const { user } = useAuthStore();
  const [cards, setCards] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedCard, setSelectedCard] = useState(null);
  const [showMemberModal, setShowMemberModal] = useState(false);

  // Form for Member
  const [memberName, setMemberName] = useState('');
  const [memberRelation, setMemberRelation] = useState('');
  const [memberAge, setMemberAge] = useState('');
  const [memberGender, setMemberGender] = useState('MALE');
  const [error, setError] = useState('');

  // Card search
  const [cardNumberSearch, setCardNumberSearch] = useState('');

  useEffect(() => {
    fetchCards();
  }, []);

  const fetchCards = async () => {
    setLoading(true);
    try {
      const res = await healthCardApi.getHealthCards({ page: 0, size: 100 });
      setCards(res.data.data?.content || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchCard = async (e) => {
    e.preventDefault();
    if (!cardNumberSearch) {
      fetchCards();
      return;
    }
    setLoading(true);
    try {
      const res = await healthCardApi.getHealthCardByNumber(cardNumberSearch.trim());
      setCards(res.data.data ? [res.data.data] : []);
    } catch (err) {
      setCards([]);
    } finally {
      setLoading(false);
    }
  };

  const handleAddMember = async (e) => {
    e.preventDefault();
    setError('');
    const payload = {
      name: memberName,
      relation: memberRelation,
      age: Number(memberAge),
      gender: memberGender
    };
    try {
      const res = await healthCardApi.addFamilyMember(selectedCard.id, payload);
      setSelectedCard(res.data.data);
      setShowMemberModal(false);
      resetMemberForm();
      fetchCards();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add member');
    }
  };

  const handleRemoveMember = async (memberId) => {
    if (!window.confirm('Are you sure you want to remove this family member?')) return;
    try {
      await healthCardApi.removeFamilyMember(selectedCard.id, memberId);
      // reload card details
      const res = await healthCardApi.getHealthCardByNumber(selectedCard.cardNumber);
      setSelectedCard(res.data.data);
      fetchCards();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to remove member');
    }
  };

  const resetMemberForm = () => {
    setMemberName('');
    setMemberRelation('');
    setMemberAge('');
    setMemberGender('MALE');
    setError('');
  };

  return (
    <div className="animate-fade-in p-6">
      <PageHeader 
        title="Digital Health Cards" 
        description="View issued digital health cards and family members mapping"
      />

      {/* Search bar */}
      <form onSubmit={handleSearchCard} className="relative flex-1 mb-6 max-w-md">
        <Search className="absolute left-3 top-3.5 w-4 h-4 text-surface-400" />
        <input 
          type="text" 
          placeholder="Search by card number..." 
          value={cardNumberSearch}
          onChange={(e) => setCardNumberSearch(e.target.value)}
          className="w-full pl-10 pr-4 py-2.5 border border-surface-200 rounded-xl text-sm focus:ring-primary-500"
        />
      </form>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left list */}
        <div className="lg:col-span-2 space-y-4">
          {loading ? (
            <div className="flex justify-center items-center h-64">
              <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-600"></div>
            </div>
          ) : (
            <div className="bg-white rounded-2xl border border-surface-200/60 shadow-card overflow-hidden">
              <table className="min-w-full divide-y divide-surface-200">
                <thead className="bg-surface-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Card Number</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Patient Name</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold text-surface-500 uppercase">Issued Store</th>
                    <th className="px-6 py-3 text-right text-xs font-semibold text-surface-500 uppercase">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-200 bg-white">
                  {cards.map((c) => (
                    <tr key={c.id} className={`hover:bg-surface-50/50 transition-colors ${
                      selectedCard?.id === c.id ? 'bg-primary-50/30' : ''
                    }`}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-primary-600">
                        {c.cardNumber}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-surface-900">
                        {c.patientName}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-surface-500">
                        {c.storeName}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <button 
                          onClick={() => setSelectedCard(c)}
                          className="flex items-center gap-1.5 ml-auto px-2.5 py-1.5 bg-surface-100 hover:bg-surface-200 text-surface-700 rounded-lg text-xs font-semibold cursor-pointer"
                        >
                          <Eye className="w-3.5 h-3.5" />
                          View Details
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Right card preview & members */}
        <div className="space-y-4">
          {selectedCard ? (
            <div className="space-y-4">
              {/* Premium digital card preview */}
              <div className="bg-gradient-to-br from-primary-600 via-primary-700 to-indigo-800 rounded-2xl p-6 text-white shadow-xl relative overflow-hidden">
                <div className="absolute right-0 top-0 w-48 h-48 bg-white/5 rounded-full -translate-y-1/3 translate-x-1/3" />
                <div className="relative z-10 flex flex-col justify-between h-36">
                  <div className="flex justify-between items-start">
                    <div>
                      <h4 className="text-xs uppercase tracking-wider opacity-80">Apna Swasthya Kendra</h4>
                      <h3 className="text-lg font-bold">DIGITAL HEALTH CARD</h3>
                    </div>
                    <CreditCard className="w-8 h-8 opacity-80" />
                  </div>
                  <div>
                    <p className="text-2xl font-bold tracking-widest">{selectedCard.cardNumber}</p>
                    <div className="flex justify-between items-end mt-4">
                      <div>
                        <p className="text-[10px] uppercase opacity-75">Holder</p>
                        <p className="text-sm font-bold">{selectedCard.patientName}</p>
                      </div>
                      <div className="text-right">
                        <p className="text-[10px] uppercase opacity-75">Phone</p>
                        <p className="text-sm font-bold">{selectedCard.patientPhone}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Family members management */}
              <div className="bg-white rounded-2xl border border-surface-200/60 p-5 shadow-card space-y-4">
                <div className="flex justify-between items-center">
                  <h4 className="text-sm font-bold text-surface-900">Family Members</h4>
                  {selectedCard.members?.length < 5 && (
                    <button 
                      onClick={() => setShowMemberModal(true)}
                      className="flex items-center gap-1 text-xs font-bold text-primary-600 hover:text-primary-700 cursor-pointer"
                    >
                      <Plus className="w-3.5 h-3.5" /> Add
                    </button>
                  )}
                </div>

                <div className="divide-y divide-surface-150">
                  {selectedCard.members && selectedCard.members.length > 0 ? (
                    selectedCard.members.map((m) => (
                      <div key={m.id} className="py-3 flex justify-between items-center">
                        <div>
                          <div className="text-xs font-bold text-surface-900">{m.name}</div>
                          <div className="text-[10px] text-surface-500">{m.relation} • {m.gender}, {m.age} yrs</div>
                        </div>
                        <button 
                          onClick={() => handleRemoveMember(m.id)}
                          className="p-1 text-surface-400 hover:text-red-600 cursor-pointer"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    ))
                  ) : (
                    <div className="text-center py-6 text-xs text-surface-400">
                      No family members added. Add up to 5 family members.
                    </div>
                  )}
                </div>
              </div>
            </div>
          ) : (
            <div className="bg-surface-50 rounded-2xl border border-dashed border-surface-300 p-8 text-center text-xs text-surface-400">
              Select a health card from the list to view details and manage family members.
            </div>
          )}
        </div>
      </div>

      {/* Member Modal */}
      {showMemberModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-2xl border border-surface-200 shadow-xl max-w-sm w-full p-6 animate-scale-in">
            <h3 className="text-lg font-bold text-surface-900 mb-4">Add Family Member</h3>
            <form onSubmit={handleAddMember} className="space-y-4">
              {error && (
                <div className="p-3 bg-red-50 text-red-700 rounded-lg text-sm flex items-center gap-2">
                  <ShieldAlert className="w-4 h-4" />
                  {error}
                </div>
              )}
              <div>
                <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Full Name *</label>
                <input type="text" required value={memberName} onChange={(e) => setMemberName(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
              </div>
              <div>
                <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Relation *</label>
                <input type="text" placeholder="e.g. Spouse, Son, Daughter" required value={memberRelation} onChange={(e) => setMemberRelation(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Age *</label>
                  <input type="number" required min="0" max="120" value={memberAge} onChange={(e) => setMemberAge(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-surface-600 uppercase mb-1">Gender *</label>
                  <select value={memberGender} onChange={(e) => setMemberGender(e.target.value)} className="w-full border border-surface-300 rounded-lg p-2.5 text-sm">
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
              </div>
              <div className="flex justify-end gap-3 pt-4 border-t border-surface-100">
                <button type="button" onClick={() => setShowMemberModal(false)} className="px-4 py-2 border border-surface-300 rounded-lg text-sm hover:bg-surface-50 cursor-pointer">Cancel</button>
                <button type="submit" className="px-4 py-2 bg-primary-600 text-white rounded-lg text-sm hover:bg-primary-700 cursor-pointer">Add Member</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
