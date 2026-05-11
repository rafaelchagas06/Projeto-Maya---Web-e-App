import { useState, useEffect } from 'react';
import { supabase } from '../supabaseClient';
import { Search, ClipboardList, Trash2, Calendar } from 'lucide-react';

export default function Prescricoes() {
  const [prescricoes, setPrescricoes] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPrescricoes();
  }, []);

  const fetchPrescricoes = async () => {
    setLoading(true);
    // Faz um "JOIN" no Supabase para pegar os nomes em vez de só mostrar os IDs!
    const { data, error } = await supabase
      .from('prescricoes')
      .select(`
        id,
        frequencia,
        observacoes,
        created_at,
        pacientes (nome),
        exercicios (titulo, tipo)
      `)
      .order('created_at', { ascending: false });

    if (!error && data) {
      setPrescricoes(data);
    }
    setLoading(false);
  };

  const handleDelete = async (id) => {
    const confirm = window.confirm("Deseja cancelar esta prescrição? O paciente não verá mais este exercício no App.");
    if (confirm) {
      const { error } = await supabase.from('prescricoes').delete().eq('id', id);
      if (!error) {
        fetchPrescricoes(); // Atualiza a lista
      } else {
        alert("Erro ao deletar: " + error.message);
      }
    }
  };

  // Filtra pelo nome do paciente ou nome do exercício
  const filteredPrescricoes = prescricoes.filter(p => 
    p.pacientes?.nome?.toLowerCase().includes(search.toLowerCase()) || 
    p.exercicios?.titulo?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <header style={{ marginBottom: '40px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '28px', marginBottom: '8px', color: '#0F172A' }}>Histórico de Prescrições</h1>
          <p style={{ color: '#64748B' }}>Controle todos os exercícios que foram passados como dever de casa.</p>
        </div>
        <div style={{ background: 'rgba(6, 182, 212, 0.1)', padding: '12px', borderRadius: '8px', color: 'var(--primary)' }}>
          <ClipboardList size={28} />
        </div>
      </header>

      {/* Busca */}
      <div className="glass-card" style={{ padding: '24px', marginBottom: '24px', display: 'flex', gap: '16px', alignItems: 'center' }}>
        <div style={{ position: 'relative', flex: 1 }}>
          <Search size={20} style={{ position: 'absolute', left: '16px', top: '14px', color: 'var(--text-muted)' }} />
          <input 
            type="text" 
            placeholder="Pesquisar por paciente ou exercício..." 
            className="input-field" 
            style={{ paddingLeft: '48px' }}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div style={{ color: 'var(--text-muted)', fontSize: '14px', fontWeight: '500' }}>
          Total Ativas: {filteredPrescricoes.length}
        </div>
      </div>

      {/* Lista de Prescrições */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {loading ? (
          <div className="glass-card" style={{ padding: '48px', textAlign: 'center', color: 'var(--text-muted)' }}>
            Buscando histórico na nuvem...
          </div>
        ) : filteredPrescricoes.length === 0 ? (
          <div className="glass-card" style={{ padding: '48px', textAlign: 'center', color: 'var(--text-muted)' }}>
            Nenhuma prescrição encontrada. Vá ao Banco de Exercícios para prescrever!
          </div>
        ) : (
          filteredPrescricoes.map((p) => (
            <div key={p.id} className="glass-card" style={{ padding: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderLeft: '4px solid #06B6D4' }}>
              
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                  <h3 style={{ fontSize: '18px', color: 'var(--text-main)', margin: 0 }}>{p.pacientes?.nome}</h3>
                  <span style={{ fontSize: '12px', background: '#F1F5F9', color: '#64748B', padding: '4px 8px', borderRadius: '4px', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Calendar size={14} /> {new Date(p.created_at).toLocaleDateString('pt-BR')}
                  </span>
                </div>
                
                <p style={{ fontSize: '15px', fontWeight: '600', color: '#334155', marginBottom: '4px' }}>
                  {p.exercicios?.titulo}
                </p>
                <p style={{ fontSize: '14px', color: 'var(--primary)', fontWeight: '500', marginBottom: '8px' }}>
                  Frequência: {p.frequencia}
                </p>
                
                {p.observacoes && (
                  <p style={{ fontSize: '13px', color: 'var(--text-muted)', background: '#F8FAFC', padding: '8px', borderRadius: '4px', display: 'inline-block' }}>
                    {p.observacoes}
                  </p>
                )}
              </div>

              <div>
                <button 
                  onClick={() => handleDelete(p.id)}
                  style={{ background: 'transparent', border:'none', color: '#EF4444', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', fontWeight: '600', padding: '8px' }}
                >
                  <Trash2 size={18} /> Cancelar Prescrição
                </button>
              </div>

            </div>
          ))
        )}
      </div>
    </div>
  );
}