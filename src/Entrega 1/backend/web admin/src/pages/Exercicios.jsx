import { useState, useEffect } from 'react';
import { Search, Plus, PlayCircle, Image as ImageIcon, X } from 'lucide-react';
import { supabase } from '../supabaseClient'; // Importação do Supabase adicionada!

export default function Exercicios() {
  // Estados para dados reais do banco
  const [exercicios, setExercicios] = useState([]);
  const [pacientes, setPacientes] = useState([]); // Para a lista real de prescrição
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  // Estados dos Modais
  const [showModalUpload, setShowModalUpload] = useState(false);
  const [exercicioDetalhes, setExercicioDetalhes] = useState(null);
  const [exercicioPrescrever, setExercicioPrescrever] = useState(null);
  
  // Estados do formulário de Upload
  const [formTitulo, setFormTitulo] = useState('');
  const [formDescricao, setFormDescricao] = useState('');
  const [formTags, setFormTags] = useState('');
  const [arquivoUpload, setArquivoUpload] = useState(null); // Estado para o arquivo físico
  const [isUploading, setIsUploading] = useState(false); // Evita duplo clique
  
  // Estados do formulário de Prescrição
  const [formPaciente, setFormPaciente] = useState('');
  const [formFrequencia, setFormFrequencia] = useState('');
  const [formObservacoes, setFormObservacoes] = useState('');

  // Carrega os dados assim que a tela abre
  useEffect(() => {
    fetchExercicios();
    fetchPacientes();
  }, []);

  const fetchExercicios = async () => {
    setLoading(true);
    const { data, error } = await supabase.from('exercicios').select('*').order('id', { ascending: false });
    if (!error && data) {
      setExercicios(data);
    }
    setLoading(false);
  };

  const fetchPacientes = async () => {
    const { data, error } = await supabase.from('pacientes').select('id, nome').order('nome', { ascending: true });
    if (!error && data) {
      setPacientes(data);
    }
  };

  // UPLOAD REAL PARA O SUPABASE STORAGE + SALVAR NO BANCO
  const handleSaveExercicio = async (e) => {
    e.preventDefault();
    if (!arquivoUpload) return alert('Por favor, selecione um arquivo de mídia (Vídeo ou Imagem)!');
    
    setIsUploading(true);

    try {
      // 1. Gera um nome único para não dar conflito no Storage
      const fileExt = arquivoUpload.name.split('.').pop();
      const fileName = `${Math.random()}.${fileExt}`;
      const tipoArquivo = arquivoUpload.type.startsWith('video') ? 'video' : 'imagem';

      // 2. Faz o upload para o Bucket 'midias'
      const { error: uploadError } = await supabase.storage
        .from('midias')
        .upload(fileName, arquivoUpload);

      if (uploadError) throw uploadError;

      // 3. Pega o Link Público da mídia gerada
      const { data: publicUrlData } = supabase.storage.from('midias').getPublicUrl(fileName);
      const urlDaMidia = publicUrlData.publicUrl;

      // 4. Salva as informações de texto + o Link na tabela 'exercicios'
      const { error: dbError } = await supabase.from('exercicios').insert([{
        titulo: formTitulo,
        descricao: formDescricao,
        tags: formTags,
        tipo: tipoArquivo,
        url: urlDaMidia
      }]);

      if (dbError) throw dbError;

      // 5. Sucesso! Fecha o modal e atualiza a lista
      setShowModalUpload(false);
      setFormTitulo('');
      setFormDescricao('');
      setFormTags('');
      setArquivoUpload(null);
      fetchExercicios();

    } catch (error) {
      alert("Erro ao realizar upload: " + error.message);
    } finally {
      setIsUploading(false);
    }
  };

  const handlePrescrever = (e) => {
    e.preventDefault();
    // Em uma versão futura, isso faria um insert numa tabela "prescricoes"
    alert(`Exercício "${exercicioPrescrever.titulo}" prescrito para o paciente selecionado com sucesso!`);
    setExercicioPrescrever(null);
    setFormPaciente('');
    setFormFrequencia('');
    setFormObservacoes('');
  };

  const filteredExercicios = exercicios.filter(ex => 
    ex.titulo?.toLowerCase().includes(search.toLowerCase()) || 
    ex.tags?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <header style={{ marginBottom: '40px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '28px', marginBottom: '8px' }}>Banco de Exercícios</h1>
          <p style={{ color: 'var(--text-muted)' }}>Catálogo visual de mídias (Vídeos/Fotos) para envio aos planos de RPG.</p>
        </div>
        <button 
          onClick={() => setShowModalUpload(true)}
          style={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: '8px', 
            background: '#06B6D4',
            color: '#FFFFFF', 
            border: 'none', 
            padding: '12px 24px', 
            borderRadius: '8px', 
            fontSize: '14px',
            fontWeight: '600', 
            cursor: 'pointer',
            boxShadow: '0 4px 12px rgba(6, 182, 212, 0.3)',
            transition: 'all 0.2s'
          }}
          onMouseOver={(e) => e.currentTarget.style.background = '#0891B2'} 
          onMouseOut={(e) => e.currentTarget.style.background = '#06B6D4'}
        >
          <Plus size={20} /> Efetuar Upload de Mídia
        </button>
      </header>

      {/* Busca */}
      <div className="glass-card" style={{ padding: '24px', marginBottom: '32px', display: 'flex', gap: '16px', alignItems: 'center' }}>
        <div style={{ position: 'relative', flex: 1 }}>
          <Search size={20} style={{ position: 'absolute', left: '16px', top: '14px', color: 'var(--text-muted)' }} />
          <input 
            type="text" 
            placeholder="Buscar exercício no catálogo ou pesquisar por tag (ex: 'dor', 'postura')..." 
            className="input-field" 
            style={{ paddingLeft: '48px' }}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      {/* Galeria de Exercícios (Grid de Cards) */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '24px' }}>
        {loading ? (
          <p style={{ color: 'var(--text-muted)' }}>Carregando banco de mídias...</p>
        ) : filteredExercicios.length === 0 ? (
          <p style={{ color: 'var(--text-muted)' }}>Nenhum exercício encontrado. Faça o upload do primeiro padrão!</p>
        ) : (
          filteredExercicios.map(ex => (
            <div key={ex.id} className="glass-card" style={{ overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
              
              {/* Renderização da Mídia */}
              <div style={{ height: '160px', background: '#1E293B', display: 'flex', alignItems: 'center', justifyContent: 'center', position: 'relative' }}>
                 {ex.tipo === 'video' ? (
                   <video src={ex.url} controls style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                 ) : (
                   <img src={ex.url} alt={ex.titulo} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                 )}
                 <span style={{ position: 'absolute', top: '12px', right: '12px', background: 'rgba(0,0,0,0.6)', color: '#FFF', padding: '4px 8px', borderRadius: '4px', fontSize: '11px', fontWeight: 'bold' }}>
                   {ex.tipo.toUpperCase()}
                 </span>
              </div>

              <div style={{ padding: '20px', flex: 1 }}>
                <h3 style={{ fontSize: '16px', marginBottom: '8px', color: 'var(--text-main)', lineHeight: '1.4' }}>{ex.titulo}</h3>
                <p style={{ fontSize: '12px', color: 'var(--primary)', fontWeight: '600' }}>#{ex.tags?.split(', ').join(' #')}</p>
              </div>
              <div style={{ padding: '16px 20px', borderTop: '1px solid var(--border-light)', display: 'flex', justifyContent: 'space-between' }}>
                <button onClick={() => setExercicioDetalhes(ex)} style={{ background: 'none', border:'none', color: '#3B82F6', fontWeight: '500', cursor: 'pointer', fontSize: '13px' }}>
                  Ver Detalhes
                </button>
                <button onClick={() => setExercicioPrescrever(ex)} style={{ background: 'none', border:'none', color: 'var(--text-main)', fontWeight: '600', cursor: 'pointer', fontSize: '13px' }}>
                  Prescrever
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {/* ---------------- MODAIS ---------------- */}

      {/* Modal Upload */}
      {showModalUpload && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100 }}>
          <div className="glass-card" style={{ width: '100%', maxWidth: '500px', padding: '32px', background: '#FFF' }}>
            <h2 style={{ marginBottom: '24px' }}>Cadastrar Novo Padrão de Exercício</h2>
            <form onSubmit={handleSaveExercicio} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label className="label-text">Título Comercial do Exercício</label>
                <input required type="text" className="input-field" value={formTitulo} onChange={e => setFormTitulo(e.target.value)} />
              </div>
              <div>
                <label className="label-text">Descrição Completa</label>
                <textarea required className="input-field" style={{ minHeight: '80px', resize: 'vertical' }} value={formDescricao} onChange={e => setFormDescricao(e.target.value)} />
              </div>
              <div>
                <label className="label-text">Tags Relacionadas (Ex: RPG, Cervical)</label>
                <input required type="text" className="input-field" value={formTags} onChange={e => setFormTags(e.target.value)} />
              </div>
              
              {/* CAMPO DE SELEÇÃO DE ARQUIVO INSERIDO AQUI */}
              <div>
                <label className="label-text">Selecione o Arquivo (.MP4, .JPG, .PNG)</label>
                <input 
                  required 
                  type="file" 
                  accept="video/mp4, video/webm, image/png, image/jpeg" 
                  onChange={(e) => setArquivoUpload(e.target.files[0])}
                  style={{ width: '100%', padding: '10px', border: '1px solid #E2E8F0', borderRadius: '8px', background: '#F8FAFC', cursor: 'pointer' }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '24px' }}>
                <button 
                  type="button" 
                  disabled={isUploading}
                  style={{ 
                    background: '#F1F5F9', color: '#64748B', border: 'none', padding: '10px 20px', borderRadius: '8px', fontSize: '14px', fontWeight: '600', cursor: isUploading ? 'not-allowed' : 'pointer', transition: 'all 0.2s'
                  }} 
                  onMouseOver={(e) => !isUploading && (e.currentTarget.style.background = '#E2E8F0')} 
                  onMouseOut={(e) => !isUploading && (e.currentTarget.style.background = '#F1F5F9')}
                  onClick={() => setShowModalUpload(false)}
                >
                  Cancelar
                </button>

                <button 
                  type="submit" 
                  disabled={isUploading}
                  style={{ 
                    display: 'flex', alignItems: 'center', gap: '8px', 
                    background: isUploading ? '#94A3B8' : '#06B6D4', 
                    color: '#FFFFFF', border: 'none', padding: '10px 24px', borderRadius: '8px', fontSize: '14px', fontWeight: '600', cursor: isUploading ? 'not-allowed' : 'pointer', boxShadow: isUploading ? 'none' : '0 4px 12px rgba(6, 182, 212, 0.3)', transition: 'all 0.2s'
                  }}
                  onMouseOver={(e) => !isUploading && (e.currentTarget.style.background = '#0891B2')} 
                  onMouseOut={(e) => !isUploading && (e.currentTarget.style.background = '#06B6D4')}
                >
                  {isUploading ? 'Enviando ao Servidor...' : 'Realizar Upload'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Ver Detalhes */}
      {exercicioDetalhes && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100 }}>
          <div className="glass-card" style={{ width: '100%', maxWidth: '600px', padding: '0', background: '#FFF', overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
            <div style={{ height: '300px', background: '#000', position: 'relative' }}>
               <button onClick={() => setExercicioDetalhes(null)} style={{ position: 'absolute', top: '16px', right: '16px', background: 'rgba(0,0,0,0.5)', color: '#FFF', border: 'none', borderRadius: '50%', padding: '8px', cursor: 'pointer', zIndex: 10 }}>
                 <X size={20} />
               </button>
               {exercicioDetalhes.tipo === 'video' ? (
                 <video src={exercicioDetalhes.url} controls autoPlay style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
               ) : (
                 <img src={exercicioDetalhes.url} alt={exercicioDetalhes.titulo} style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
               )}
            </div>
            <div style={{ padding: '32px' }}>
              <h2 style={{ fontSize: '24px', marginBottom: '8px', color: '#0F172A' }}>{exercicioDetalhes.titulo}</h2>
              <p style={{ fontSize: '14px', color: 'var(--primary)', fontWeight: '600', marginBottom: '16px' }}>#{exercicioDetalhes.tags?.split(', ').join(' #')}</p>
              <p style={{ color: '#475569', lineHeight: '1.6' }}>{exercicioDetalhes.descricao}</p>
              
              <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '32px' }}>
                <button 
                  onClick={() => { 
                    setExercicioPrescrever(exercicioDetalhes); 
                    setExercicioDetalhes(null); 
                  }} 
                  style={{ 
                    background: '#06B6D4', color: '#FFFFFF', border: 'none', padding: '10px 24px', borderRadius: '8px', fontSize: '14px', fontWeight: '600', cursor: 'pointer', boxShadow: '0 4px 12px rgba(6, 182, 212, 0.3)', transition: 'all 0.2s'
                  }}
                  onMouseOver={(e) => e.currentTarget.style.background = '#0891B2'} 
                  onMouseOut={(e) => e.currentTarget.style.background = '#06B6D4'}
                >
                  Prescrever este Exercício
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal Prescrever */}
      {exercicioPrescrever && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100 }}>
          <div className="glass-card" style={{ width: '100%', maxWidth: '500px', padding: '32px', background: '#FFF' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
              <h2>Prescrever Exercício</h2>
              <button onClick={() => setExercicioPrescrever(null)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}><X size={24} color="#64748B" /></button>
            </div>
            
            <div style={{ background: '#F8FAFC', padding: '16px', borderRadius: '8px', marginBottom: '24px', borderLeft: '4px solid var(--primary)' }}>
              <p style={{ fontSize: '14px', fontWeight: 'bold', color: '#334155' }}>{exercicioPrescrever.titulo}</p>
              <p style={{ fontSize: '12px', color: '#64748B' }}>Defina os parâmetros para o paciente.</p>
            </div>

            <form onSubmit={handlePrescrever} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label className="label-text">Selecione o Paciente (Dados Reais)</label>
                <select required className="input-field" value={formPaciente} onChange={e => setFormPaciente(e.target.value)}>
                  <option value="">Selecione...</option>
                  {pacientes.map(paciente => (
                    <option key={paciente.id} value={paciente.id}>{paciente.nome}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="label-text">Frequência Semanal / Diária</label>
                <input required type="text" className="input-field" placeholder="Ex: 3x na semana, 2 séries de 15" value={formFrequencia} onChange={e => setFormFrequencia(e.target.value)} />
              </div>
              <div>
                <label className="label-text">Orientações e Cuidados Adicionais</label>
                <textarea className="input-field" style={{ minHeight: '80px', resize: 'vertical' }} placeholder="Ex: Fazer em frente ao espelho, parar se sentir dor aguda..." value={formObservacoes} onChange={e => setFormObservacoes(e.target.value)} />
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '24px' }}>
                <button 
                  type="button" 
                  style={{ 
                    background: '#F1F5F9', color: '#64748B', border: 'none', padding: '10px 20px', borderRadius: '8px', fontSize: '14px', fontWeight: '600', cursor: 'pointer', transition: 'all 0.2s'
                  }} 
                  onMouseOver={(e) => e.currentTarget.style.background = '#E2E8F0'} 
                  onMouseOut={(e) => e.currentTarget.style.background = '#F1F5F9'}
                  onClick={() => setExercicioPrescrever(null)}
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
                  Confirmar Prescrição
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}