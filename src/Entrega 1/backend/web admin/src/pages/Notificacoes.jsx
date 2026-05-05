import { useState, useEffect } from 'react';
import { supabase } from '../supabaseClient';
import { Bell, Send, User, Calendar, Dumbbell } from 'lucide-react';

export default function Notificacoes() {
  const [pacientes, setPacientes] = useState([]);
  const [pacienteId, setPacienteId] = useState('');
  const [titulo, setTitulo] = useState('');
  const [mensagem, setMensagem] = useState('');
  const [tipo, setTipo] = useState('Geral');
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    const fetchPacientes = async () => {
      const { data } = await supabase.from('pacientes').select('id, nome').eq('status', 'Ativo');
      if (data) setPacientes(data);
    };
    fetchPacientes();
  }, []);

  const handleEnviar = async (e) => {
    e.preventDefault();
    setEnviando(true);

    const { error } = await supabase.from('notificacoes').insert([{
      paciente_id: pacienteId,
      titulo,
      mensagem,
      tipo
    }]);

    if (!error) {
      alert("Notificação enviada com sucesso para o App Mobile!");
      setTitulo('');
      setMensagem('');
    } else {
      alert("Erro ao enviar: " + error.message);
    }
    setEnviando(false);
  };

  return (
    <div style={{ maxWidth: '800px' }}>
      <header style={{ marginBottom: '40px' }}>
        <h1 style={{ fontSize: '28px', marginBottom: '8px' }}>Central de Notificações</h1>
        <p style={{ color: 'var(--text-muted)' }}>Envie lembretes de exercícios e consultas para o App dos pacientes.</p>
      </header>

      <div className="glass-card" style={{ padding: '32px' }}>
        <form onSubmit={handleEnviar} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          
          <div>
            <label className="label-text">Selecionar Destinatário</label>
            <select required className="input-field" value={pacienteId} onChange={e => setPacienteId(e.target.value)}>
              <option value="">Selecione um paciente ativo...</option>
              {pacientes.map(p => <option key={p.id} value={p.id}>{p.nome}</option>)}
            </select>
          </div>

          <div>
            <label className="label-text">Tipo de Alerta</label>
            <div style={{ display: 'flex', gap: '12px' }}>
              <button type="button" onClick={() => setTipo('Consulta')} style={{ flex: 1, padding: '10px', borderRadius: '8px', border: '1px solid #E2E8F0', background: tipo === 'Consulta' ? '#E0F2FE' : '#FFF', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
                <Calendar size={18} color="#0EA5E9" /> Consulta
              </button>
              <button type="button" onClick={() => setTipo('Exercicio')} style={{ flex: 1, padding: '10px', borderRadius: '8px', border: '1px solid #E2E8F0', background: tipo === 'Exercicio' ? '#DCFCE7' : '#FFF', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
                <Dumbbell size={18} color="#22C55E" /> Exercício
              </button>
              <button type="button" onClick={() => setTipo('Geral')} style={{ flex: 1, padding: '10px', borderRadius: '8px', border: '1px solid #E2E8F0', background: tipo === 'Geral' ? '#F1F5F9' : '#FFF', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
                <Bell size={18} color="#64748B" /> Geral
              </button>
            </div>
          </div>

          <div>
            <label className="label-text">Título da Notificação</label>
            <input required type="text" className="input-field" placeholder="Ex: Lembrete de Consulta Amanhã" value={titulo} onChange={e => setTitulo(e.target.value)} />
          </div>

          <div>
            <label className="label-text">Mensagem Detalhada</label>
            <textarea required className="input-field" style={{ minHeight: '100px' }} placeholder="Olá! Não esqueça de realizar seus exercícios de cervical hoje..." value={mensagem} onChange={e => setMensagem(e.target.value)} />
          </div>

          <button type="submit" disabled={enviando} className="btn-primary" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px', padding: '14px' }}>
            <Send size={18} /> {enviando ? 'Enviando...' : 'Disparar Notificação'}
          </button>
        </form>
      </div>
    </div>
  );
}