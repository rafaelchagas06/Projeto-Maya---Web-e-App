import { createClient } from '@supabase/supabase-js';

const supabaseUrl = 'https://gzxqsxjluahixavoklhd.supabase.co';
const supabaseKey = 'sb_publishable_gblVzVE_U0seUjRRB9EK7g_gpb9hz5A';
export const supabase = createClient(supabaseUrl, supabaseKey);
