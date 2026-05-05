import { useState, useEffect } from 'react';
import { Search, FileText, Calendar, Plus } from 'lucide-react';
import { supabase } from '../supabaseClient'; // Conexão com o banco!

export default function Prontuarios() {
  // Estados para dados reais do banco
  const [pacientes, setPacientes] = useState([]);
  const [prontuarios, setProntuarios] = useState([]); // Agora carrega só o histórico do paciente selecionado
  const [selectedPacienteId, setSelectedPacienteId] = useState('');
  const [loading, setLoading] = useState(true);

  // Estados do Modal
  const [showNovaSessao, setShowNovaSessao] = useState(false);
  const [novaData, setNovaData] = useState('');
  const [novaDor, setNovaDor] = useState(5);
  const [novaObs, setNovaObs] = useState('');

  // 1. Busca os pacientes assim que a tela abre
  useEffect(() => {
    fetchPacientes();
  }, []);

  // 2. Busca os prontuários SEMPRE que o paciente selecionado mudar
  useEffect(() => {
    if (selectedPacienteId) {
      fetchProntuarios(selectedPacienteId);
    } else {
      setProntuarios([]);
    }
  }, [selectedPacienteId]);

  const fetchPacientes = async () => {
    const { data, error } = await supabase.from('pacientes').select('*').order('nome', { ascending: true });
    if (!error && data) {
      setPacientes(data);
      if (data.length > 0) {
        setSelectedPacienteId(data[0].id); // Auto-seleciona o primeiro paciente da lista
      }
    }
  };

  const fetchProntuarios = async (pacienteId) => {
    setLoading(true);
    const { data, error } = await supabase
      .from('prontuarios')
      .select('*')
      .eq('paciente_id', pacienteId)
      .order('data', { ascending: false }); // Traz as sessões mais recentes primeiro

    if (!error && data) {
      setProntuarios(data);
    }
    setLoading(false);
  };

  // 3. SALVAR NOVA SESSÃO NO SUPABASE
  const handleSalvarSessao = async (e) => {
    e.preventDefault();

    if (!selectedPacienteId) {
      alert("Por favor, selecione um paciente primeiro.");
      return;
    }

    const { error } = await supabase.from('prontuarios').insert([{
      paciente_id: selectedPacienteId,
      data: novaData,
      dor: Number(novaDor),
      observacao: novaObs
    }]);

    if (!error) {
      fetchProntuarios(selectedPacienteId); // Atualiza a lista na hora
      setShowNovaSessao(false);
      setNovaData('');
      setNovaDor(5);
      setNovaObs('');
    } else {
      alert("Erro ao salvar prontuário: " + error.message);
    }
  };

  const selectedPaciente = pacientes.find(p => p.id === Number(selectedPacienteId));

  return (
    <div style={{ display: 'flex', gap: '32px' }}>
      {/* Coluna Esquerda: Sessões do Paciente */}
      <div style={{ flex: 1 }}>
        <header style={{ marginBottom: '40px' }}>
          <h1 style={{ fontSize: '28px', marginBottom: '8px' }}>Prontuário Eletrônico</h1>
          <p style={{ color: 'var(--text-muted)' }}>Histórico clínico e evolução por sessão.</p>
        </header>

        <div className="glass-card" style={{ padding: '24px', marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '16px' }}>
          <FileText size={24} color="var(--primary)" />
          <div style={{ flex: 1 }}>
            <label className="label-text">Selecione o Paciente para visualizar a Ficha:</label>
            <select 
              className="input-field" 
              value={selectedPacienteId} 
              onChange={e => setSelectedPacienteId(e.target.value)}
              disabled={pacientes.length === 0}
            >
              {pacientes.length === 0 ? (
                <option value="">Nenhum paciente cadastrado...</option>
              ) : (
                pacientes.map(p => (
                  <option key={p.id} value={p.id}>{p.nome}</option>
                ))
              )}
            </select>
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
          <h2 style={{ fontSize: '20px' }}>Histórico de Consultas</h2>
          <button 
            style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: '8px', 
              background: '#10B981', 
              color: 'white', 
              border: 'none', 
              padding: '10px 20px', 
              borderRadius: '8px', 
              fontSize: '14px', 
              fontWeight: '600', 
              cursor: pacientes.length === 0 ? 'not-allowed' : 'pointer',
              opacity: pacientes.length === 0 ? 0.5 : 1,
              transition: 'all 0.2s'
            }} 
            onMouseOver={(e) => pacientes.length > 0 && (e.currentTarget.style.background = '#059669')} 
            onMouseOut={(e) => pacientes.length > 0 && (e.currentTarget.style.background = '#10B981')}
            onClick={() => setShowNovaSessao(true)}
            disabled={pacientes.length === 0}
          >
            <Plus size={18} /> Cadastrar Evolução
          </button>
        </div>

        {loading ? (
          <div className="glass-card" style={{ padding: '48px', textAlign: 'center', color: 'var(--text-muted)' }}>Carregando histórico...</div>
        ) : prontuarios.length === 0 ? (
          <div className="glass-card" style={{ padding: '48px', textAlign: 'center', color: 'var(--text-muted)' }}>Nenhuma sessão registrada. Crie a evolução primária clicando acima.</div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {prontuarios.map(h => (
              <div key={h.id} className="glass-card" style={{ padding: '24px', borderLeft: `4px solid ${h.dor > 6 ? '#EF4444' : h.dor > 3 ? '#F59E0B' : '#10B981'}`, background: '#fff' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--primary-dark)', fontWeight: 'bold' }}>
                    <Calendar size={18} /> {new Date(h.data).toLocaleDateString('pt-BR', {timeZone: 'UTC'})}
                  </div>
                  <div style={{ background: '#F1F5F9', padding: '6px 12px', borderRadius: '16px', fontSize: '11px', fontWeight: 'bold', border: '1px solid #E2E8F0' }}>
                    Dor Relatada Escala: {h.dor}/10
                  </div>
                </div>
                <p style={{ color: 'var(--text-main)', lineHeight: '1.6', fontSize: '15px' }}>{h.observacao}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Coluna Direita: Resumo do Paciente Selecionado */}
      <div style={{ width: '320px', display: 'flex', flexDirection: 'column', gap: '24px', marginTop: '108px' }}>
         <div className="glass-card" style={{ padding: '32px 24px', background: 'var(--primary)', color: 'white', borderRadius: '24px' }}>
            <div style={{ width: '64px', height: '64px', background: 'rgba(255,255,255,0.2)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '24px', fontWeight: 'bold', marginBottom: '16px' }}>
               {selectedPaciente?.nome ? selectedPaciente.nome.charAt(0).toUpperCase() : '?'}
            </div>
            <h3 style={{ fontSize: '20px', marginBottom: '4px', color: 'white' }}>{selectedPaciente?.nome || 'Selecione um paciente'}</h3>
            <p style={{ fontSize: '12px', opacity: 0.8, marginBottom: '24px' }}>{selectedPaciente?.email || '---'}</p>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid rgba(255,255,255,0.2)', paddingTop: '16px' }}>
               <div>
                 <p style={{ fontSize: '10px', opacity: 0.8, textTransform: 'uppercase', fontWeight: 'bold', letterSpacing: '1px' }}>Total Sessões</p>
                 <p style={{ fontSize: '24px', fontWeight: 'bold', marginTop: '4px' }}>{prontuarios.length}</p>
               </div>
               <div style={{ textAlign: 'right' }}>
                 <p style={{ fontSize: '10px', opacity: 0.8, textTransform: 'uppercase', fontWeight: 'bold', letterSpacing: '1px' }}>Dor Atualização</p>
                 <p style={{ fontSize: '24px', fontWeight: 'bold', marginTop: '4px' }}>{prontuarios[0]?.dor ?? '-'}<span style={{fontSize:'12px', opacity:0.8}}>/10</span></p>
               </div>
            </div>
         </div>
      </div>

      {/* Modal Nova Sessão */}
      {showNovaSessao && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 50 }}>
          <div className="glass-card" style={{ padding: '32px', width: '100%', maxWidth: '500px', background: 'white' }}>
            <h2 style={{ marginBottom: '24px' }}>Registrar Evolução Clínica</h2>
            <form onSubmit={handleSalvarSessao} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label className="label-text">Data da Sessão no Consultório</label>
                <input required type="date" className="input-field" value={novaData} onChange={e => setNovaData(e.target.value)} />
              </div>
              <div>
                <label className="label-text">Nível de Dor da Queixa (Escala Analógica de 0 a 10)</label>
                <input required type="number" min="0" max="10" className="input-field" value={novaDor} onChange={e => setNovaDor(e.target.value)} />
              </div>
              <div>
                <label className="label-text">Observações / Conduta Realizada hoje</label>
                <textarea required className="input-field" style={{ minHeight: '120px', resize: 'vertical' }} value={novaObs} onChange={e => setNovaObs(e.target.value)} placeholder="Paciente referiu melhora na mobilidade..." />
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '24px' }}>
                <button 
                  type="button" 
                  style={{ 
                    background: '#F1F5F9', color: '#64748B', border: 'none', padding: '10px 20px', borderRadius: '8px', fontSize: '14px', fontWeight: '600', cursor: 'pointer', transition: 'all 0.2s'
                  }} 
                  onMouseOver={(e) => e.currentTarget.style.background = '#E2E8F0'} 
                  onMouseOut={(e) => e.currentTarget.style.background = '#F1F5F9'}
                  onClick={() => setShowNovaSessao(false)}
                >
                  Cancelar
                </button>

                <button 
                  type="submit" 
                  style={{ 
                    background: '#06B6D4', color: '#FFFFFF', border: 'none', padding: '10px 24px', borderRadius: '8px', fontSize: '14px', fontWeight: '600', cursor: 'pointer', boxShadow: '0 4px 12px rgba(6, 182, 212, 0.3)', transition: 'all 0.2s'
                  }}
                  onMouseOver={(e) => e.currentTarget.style.background = '#0891B2'} 
                  onMouseOut={(e) => e.currentTarget.style.background = '#06B6D4'}
                >
                  Registrar Prontuário
                </button>
              </div>

            </form>
          </div>
        </div>
      )}
    </div>
  );
}